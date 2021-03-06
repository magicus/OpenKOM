/*
 * Created on Nov 3, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import nu.rydin.kom.backend.data.FileManager;
import nu.rydin.kom.backend.data.SettingsManager;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.SettingKeys;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.events.UserAttendanceEvent;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.ModuleException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.ClientSettings;
import nu.rydin.kom.modules.Module;
import nu.rydin.kom.structs.IntrusionAttempt;
import nu.rydin.kom.structs.SessionListItem;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.utils.Base64;
import nu.rydin.kom.utils.FileUtils;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ServerSessionFactoryImpl implements ServerSessionFactory, Module
{
    private int m_nextSessionId = 0;
    
    private Map<String, IntrusionAttempt> intrusionAttempts = Collections.synchronizedMap(new HashMap<String, IntrusionAttempt>());
    
	private SessionManager m_sessionManager;
	
	/**
	 * Valid tickets
	 */
	private Map<String, Long> m_validTickets = Collections.synchronizedMap(new HashMap<String, Long>());

	/**
	 * General purpose timer for ticket expirations and intrusion attempt clearing
	 */
	private Timer timer = new Timer(true);
	
	private class TicketKiller extends TimerTask
	{
	    private String m_ticket;
	    
	    public TicketKiller(String ticket)
	    {
	        m_ticket = ticket;
	    }
	    
	    public void run()
	    {
	        if(ServerSessionFactoryImpl.this.m_validTickets.remove(m_ticket) != null)
	            Logger.info(this, "Discarded unused ticket");
	    }
	}
	
	private class IntrusionKiller extends TimerTask
	{
        private String client;
        
        public IntrusionKiller(String client)
        {
            this.client = client;
        }
        
        public void run()
        {
            synchronized(ServerSessionFactoryImpl.this.intrusionAttempts)
            {
                IntrusionAttempt ia = ServerSessionFactoryImpl.this.intrusionAttempts.get(client);
                if(ia != null)
                {
                    // Decrease attempt count and remove if it reached zero
                    //
                    if(ia.expireAttempt() == 0)
                    {
                        intrusionAttempts.remove(client);
                        Logger.info(this, "Discarded expired intrusion attempt for: " + client);
                    }
                }
	        }
	    }
	 }

    
    private class ContextCleaner extends Thread
    {
        public void run()
        {
            try
            {
                UserContextFactory ucf = UserContextFactory.getInstance();
                for(;;)
                {
                    Thread.sleep(60000);
                    synchronized(ucf)
                    {
                        List<UserContext> list = ucf.listContexts();
                        for (UserContext each : list)
                        {
                            if(!ServerSessionFactoryImpl.this.m_sessionManager.userHasSession(each.getUserId()))
                            {
                                ucf.release(each.getUserId());
                                Logger.info(this, "Released rogue context for user " + each.getUserId());
                            }
                        }
                    }
                }
            }
            catch(InterruptedException e)
            {
                Logger.info(this, "ContextCleaner shutting down...");
            }
        }
    }
    
    private ContextCleaner m_contextCleaner;
		
	public void start(Map<String, String> properties) throws ModuleException
	{
	    // Initialize the static global server settings class.
	    //
	    ServerSettings.initialize(properties);
	    
	    // Initialize the static global client settings class.
	    // FIXME: This should probably not be initialized here, but since
	    // the module system can't handle a separated client-side anyway,
	    // we can safely ignore this for now.
	    ClientSettings.initialize(properties);
        
        // Start context cleaner thread
        //
        m_contextCleaner = new ContextCleaner();
        m_contextCleaner.setName("ContextCleaner");
        m_contextCleaner.start();
	    
	    // Since we just loaded the client settings, we can perform this check:
	    //
        // Check that we have the character sets we need
        //
        StringTokenizer st = new StringTokenizer(ClientSettings.getCharsets(), ",");
        while (st.hasMoreTokens())
        {
            String charSet = st.nextToken();
            try
            {
                new OutputStreamWriter(System.out, charSet);
            } 
            catch (UnsupportedEncodingException e)
            {
    			throw new ModuleException("Character set " + charSet + " not supported. Do you have charsets.jar in you classpath?", e);
            }
        }
	    
	    // Make sure there's at least a sysop in the database
	    //
	    boolean committed = false;
	    DataAccess da = null;
		try
		{
		    da = DataAccessPool.instance().getDataAccess();
			UserManager um = da.getUserManager();
			 
			//FIXME Move to a bootstrapping sql-script.
			// Make sure there is at least a sysop in the database.
			//
			if(!um.userExists("sysop"))
			{
				um.addUser("sysop", "sysop", "Sysop", null, "", "", "", "", "", "", "", "", "", 
					"ISO-8859-1", "sv_SE", UserFlags.DEFAULT_FLAGS1, UserFlags.DEFAULT_FLAGS2, 
				UserFlags.DEFAULT_FLAGS3, UserFlags.DEFAULT_FLAGS4, UserPermissions.EVERYTHING);
				da.commit();
				committed = true;
			}
		}
		catch(SQLException e)
		{
			throw new ModuleException(e);
		}
		catch(NoSuchAlgorithmException e)
		{
			// Could not calculate password digest. Should not happen!
			//
		    throw new ModuleException(e);
		}
		catch(AmbiguousNameException e)
		{
			// Ambigous name when adding sysop. Should not happen!
			//
		    throw new ModuleException(e);
		}
		catch(DuplicateNameException e)
		{
			// Duplicate name when adding sysop. Should not happen!
			//
		    throw new ModuleException(e);
		} 
		catch (UnexpectedException e)
        {
            // Noone expects ths Spanish Inquisition!
            //
		    throw new ModuleException(e);
        }
		finally
		{
			if(da != null)
			{
			    if (!committed)
			    {
	                try
	                {
	                    da.rollback();
	                } 
					catch (UnexpectedException e)
	                {
					    // This is probably bad if it happens.
					    throw new ModuleException(e);
	                }
			    }
			    DataAccessPool.instance().returnDataAccess(da);
			}
		}
	    
	    m_sessionManager = new SessionManager();
	    m_sessionManager.start();
	}
	
	public void stop()
	{
	    m_sessionManager.stop();
        m_contextCleaner.interrupt();
	}
	
	public void join()
	throws InterruptedException
	{
	    m_sessionManager.join();
        m_contextCleaner.join();
	}
	
	public ServerSessionFactoryImpl()
	throws UnexpectedException
	{

	}	
	
    public void notifySuccessfulLogin(String client)
    {
        intrusionAttempts.remove(client);
    }
    
    public void notifyFailedLogin(String client, int limit, long lockout)
    {
        synchronized(intrusionAttempts)
        {
            IntrusionAttempt ia = intrusionAttempts.get(client);
            if(ia == null)
            {
                ia = new IntrusionAttempt(client, limit, lockout);
                intrusionAttempts.put(client, ia);
            }
            else
                ia.addAttempt();
            
            // We schedule a timer for every attempt. When they expire, they will decrease the
            // counter, resulting in the lockout to run from the last attempt
            //
            timer.schedule(new IntrusionKiller(client), lockout);
        }   
    }
    
    public boolean isBlacklisted(String client)
    {
        synchronized(intrusionAttempts)
        {
            IntrusionAttempt ia = intrusionAttempts.get(client);
            if(ia == null)
                return false;
            return ia.isBlocked();
        }
    }
	
	public ServerSession login(String ticket, short clientType, boolean allowMulti)
	throws AuthenticationException, LoginProhibitedException, AlreadyLoggedInException, UnexpectedException
	{
	    // Log us in!
	    //
	    DataAccess da = DataAccessPool.instance().getDataAccess();
	    try
	    {
	        long id = this.authenticate(ticket);
	        ServerSession session = this.innerLogin(id, clientType, da, allowMulti);
	        this.consumeTicket(ticket);
	        return session;
	    }
	    finally
	    {
	        DataAccessPool.instance().returnDataAccess(da);
	    }
	}
	
	private ServerSession innerLogin(long id, short clientType, DataAccess da, boolean allowMulti)
	throws AuthenticationException, LoginProhibitedException, AlreadyLoggedInException, UnexpectedException
	{
		try
		{	 
			// Authenticate user
			//
		    UserManager um = da.getUserManager();
			UserInfo ui = um.loadUser(id);
			
			// Login prohibited? Allow login only if sysop
			//
			if(!m_sessionManager.canLogin() && (ui.getRights() & UserPermissions.ADMIN) == 0)
			    throw new LoginProhibitedException();
			
			// Was the user already logged in?
			//
            List<ServerSession> userSessions = m_sessionManager.getSessionsByUser(id);
            int top = userSessions.size();
			if(!allowMulti && top > 0)
            {
                ArrayList<SessionListItem> list = new ArrayList<SessionListItem>(top);
                for(Iterator<ServerSession> itor = userSessions.iterator(); itor.hasNext();)
                {
                    ServerSession each = itor.next();
                    list.add(new SessionListItem(each.getSessionId(), each.getClientType(), each.getLoginTime(),
                            each.getLastHeartbeat()));
                }
				throw new AlreadyLoggedInException(list);
            }
			
			// Create a ServerSessionImpl, wrapped in a dynamic proxy and an InvocationHandler
			// keeping track of connections and transactions.
			//
			ServerSessionImpl session = new ServerSessionImpl(da, id, m_nextSessionId++, 
                    clientType, m_sessionManager);
			m_sessionManager.registerSession(session);
			
			// Successfully logged in!
			// Broadcast message.
			//
			m_sessionManager.broadcastEvent(new UserAttendanceEvent(id, ui.getName(), UserAttendanceEvent.LOGIN));

			//  Create transactional wrapper and return 
			//
			InvocationHandler handler = new TransactionalInvocationHandler(session);
			return (ServerSession) Proxy.newProxyInstance(ServerSession.class.getClassLoader(), 
				new Class[] { ServerSession.class }, handler);
				
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, e);
		}
		catch(ObjectNotFoundException e)
		{
			// User was not found. We treat that as an authentication
			// exception.
			//
			throw new AuthenticationException();
		}
	}
	
	public ServerSession login(String user, String password, short clientType, boolean allowMulti)
	throws AuthenticationException, LoginProhibitedException, AlreadyLoggedInException, UnexpectedException
	{
		DataAccess da = DataAccessPool.instance().getDataAccess();
		try
		{
			UserManager um = da.getUserManager();
			 
			// Authenticate user
			//
			long id = um.authenticate(user, password);
			return this.innerLogin(id, clientType, da, allowMulti);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, e);
		}
		catch(ObjectNotFoundException e)
		{
			// User was not found. We treat that as an authentication
			// exception.
			//
			throw new AuthenticationException();
		}
		catch(NoSuchAlgorithmException e)
		{
			// Could not calculate password digest. Should not happen!
			//
			throw new UnexpectedException(-1, e);
		}
		finally
		{
			DataAccessPool.instance().returnDataAccess(da);
		}
	}
	
	public String generateTicket(String user, String password)
	throws AuthenticationException, UnexpectedException
	{
	    // Check that username and password are valid.
	    //
	    long userId = this.authenticate(user, password);
	    try
	    {
	        // Generate 128 bytes of random data and calculate MD5
	        // digest. A ticket is typically valid for <30s, so
	        // this feels fairly secure.
	        //
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] data = new byte[128];
	        SecureRandom.getInstance("SHA1PRNG").nextBytes(data);
	        md.update(data);
	        String ticket = Base64.encodeBytes(md.digest());
	        
	        // Have a valid ticket! Now register it.
	        //
            m_validTickets.put(ticket, userId);
            timer.schedule(new TicketKiller(ticket), ServerSettings.getTicketLifetime()); 
            return ticket;
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	        throw new UnexpectedException(-1, e);
	    }

	}
	
	public void consumeTicket(String ticket) throws AuthenticationException
    {
        Long idObj = (Long) m_validTickets.remove(ticket);
        if (idObj == null)
        {
            throw new AuthenticationException();
        }
    }
	
	public long authenticate(String ticket)
	throws AuthenticationException
	{
	    Long idObj = (Long) m_validTickets.get(ticket);
	    if(idObj == null)
	    {
	        throw new AuthenticationException();
	    }
	    return idObj.longValue();
	}
	
	public long authenticate(String user, String password)
	throws AuthenticationException, UnexpectedException
	{
		DataAccess da = DataAccessPool.instance().getDataAccess();
		try
		{
			UserManager um = da.getUserManager();
			
			// Authenticate user
			//
			return um.authenticate(user, password);
		}
		catch(ObjectNotFoundException e)
		{
			// User was not found. We treat that as an authentication
			// exception.
			//
			throw new AuthenticationException();
		}
		catch(NoSuchAlgorithmException e)
		{
			// Could not calculate password digest. Should not happen!
			//
			throw new UnexpectedException(-1, e);
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, e);
		}
		finally
		{
			DataAccessPool.instance().returnDataAccess(da);
		}
	}
	
    public boolean allowsSelfRegistration()
    throws UnexpectedException
    {
        DataAccessPool dap = DataAccessPool.instance();
        DataAccess da = dap.getDataAccess();
        try
        {
            SettingsManager sm = da.getSettingManager();
            return sm.getNumber(SettingKeys.ALLOW_SELF_REGISTER) != 0;
        }
        catch(ObjectNotFoundException e)
        {
            // Not found? Not set!
            //
            return false;
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(-1, e);
        }
        finally
        {
            dap.returnDataAccess(da);
        }
    }
    
    public boolean loginExits(String userid)
    throws AuthorizationException, UnexpectedException
    {
        DataAccessPool dap = DataAccessPool.instance();
        DataAccess da = dap.getDataAccess();
        try
        {
            da.getUserManager().getUserIdByLogin(userid);
            return true;
        }
        catch(ObjectNotFoundException e)
        {
            return false;
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(-1, e);
        }        
        finally
        {
            dap.returnDataAccess(da);
        }        
    }
    
    public long selfRegister(String login, String password, String fullName, String charset)
    throws AuthorizationException, UnexpectedException, AmbiguousNameException, DuplicateNameException
    {
        DataAccessPool dap = DataAccessPool.instance();
        DataAccess da = dap.getDataAccess();
        boolean committed = false;
        try
        {
            // Are we allowed to do this?
            //
            if(!this.allowsSelfRegistration())
                throw new AuthorizationException();
            
            // Create user
            //
            long id = da.getUserManager().addUser(login, password, fullName, null, "", "", "", "", "", "", "", "", "", 
					charset, "sv_SE", UserFlags.DEFAULT_FLAGS1, UserFlags.DEFAULT_FLAGS2, 
				UserFlags.DEFAULT_FLAGS3, UserFlags.DEFAULT_FLAGS4, UserPermissions.SELF_REGISTERED_USER);
            
            // Create a login-script to help this user set stuff up
            // 
            try
            {
                String content = FileUtils.loadTextFromResource("selfregistered.login"); 
	            FileManager fm = da.getFileManager();
	            fm.store(id, ".login.cmd", content);
            }
            catch(FileNotFoundException e)
            {
                // No command file exists? Probably just means the
                // sysop doesn't think one is needed. Just skip!
            }
            catch(IOException e)
            {
                throw new UnexpectedException(-1, e);
            }            
            // Done!
            //
            da.commit();
            committed = true;
            return id;
        }
        catch(SQLException e)
        {
            throw new UnexpectedException(-1, e);
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new UnexpectedException(-1, e);
        }                
        finally
        {
            if(!committed)
                da.rollback();
            dap.returnDataAccess(da);
        }                
    }

    public void killSession(int sessionId, String user, String password) throws AuthenticationException, UnexpectedException, InterruptedException
    {
        // Authenticate
        //
        long id = this.authenticate(user, password);
        this.innerKillSession(sessionId, id);
    }
    
    public void killSession(int session, String ticket) throws AuthenticationException, UnexpectedException, InterruptedException
    {
        // Authenticate
        //
        long id = this.authenticate(ticket);
        this.innerKillSession(session, id);
    }
    
    protected void innerKillSession(int sessionId, long userId)
    throws AuthenticationException, UnexpectedException, InterruptedException
    {
        // We can only kill our own sessions
        //
        ServerSession sess = m_sessionManager.getSessionById(sessionId);
        if(sess.getLoggedInUserId() != userId)
           throw new AuthenticationException();
        m_sessionManager.killSessionById(sessionId);
    }
}
