/*
 * Created on Nov 3, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.events.UserAttendanceEvent;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Module;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.utils.Base64;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ServerSessionFactoryImpl implements ServerSessionFactory, Module
{
	private SessionManager m_sessionManager;

	private static final ServerSessionFactory s_instance;
	
	static
	{
	    try
	    {
	        s_instance = new ServerSessionFactoryImpl();
	    }
	    catch(UnexpectedException e)
	    {
	        throw new ExceptionInInitializerError(e);
	    }
	}
	
	/**
	 * Valid tickets
	 */
	private Map m_validTickets = Collections.synchronizedMap(new HashMap());

	/**
	 * Ticket expiration timer
	 */
	private Timer m_ticketExpirations = new Timer(true);
	
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
	
/*	public static ServerSessionFactory instance()
	{
		return s_instance;
	}*/
	
	public void start(Map properties)
	{
	    m_sessionManager = new SessionManager();
	    m_sessionManager.start();
	}
	
	public void stop()
	{
	    m_sessionManager.stop();
	}
	
	public void join()
	throws InterruptedException
	{
	    m_sessionManager.join();
	}
	
	public ServerSessionFactoryImpl()
	throws UnexpectedException
	{
	    // Make sure there's at least a sysop in the database
	    //
	    boolean committed = false;
		DataAccess da = DataAccessPool.instance().getDataAccess();
		try
		{
			UserManager um = da.getUserManager();
			 
			//FIXME Move to a bootstrapping sql-script.
			// Make sure there is at least a sysop in the database.
			//
			if(!um.userExists("sysop"))
			{
				um.addUser("sysop", "sysop", "Sysop", "", "", "", "", "", "", "", "", "", 
					"ISO-8859-1", "sv_SE", UserFlags.DEFAULT_FLAGS1, UserFlags.DEFAULT_FLAGS2, 
				UserFlags.DEFAULT_FLAGS3, UserFlags.DEFAULT_FLAGS4, UserPermissions.EVERYTHING);
				da.commit();
				committed = true;
			}
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, e);
		}
		catch(NoSuchAlgorithmException e)
		{
			// Could not calculate password digest. Should not happen!
			//
			throw new UnexpectedException(-1, e);
		}
		catch(AmbiguousNameException e)
		{
			// Ambigous name when adding sysop. Should not happen!
			//
			throw new UnexpectedException(-1, e);
		}
		catch(DuplicateNameException e)
		{
			// Duplicate name when adding sysop. Should not happen!
			//
			throw new UnexpectedException(-1, e);
		}
		finally
		{
			if(!committed)
				da.rollback();
			DataAccessPool.instance().returnDataAccess(da);
		}
	}	
	
	public void killSession(String user, String password)
	throws AuthenticationException, UnexpectedException, InterruptedException
	{
	    m_sessionManager.killSession(this.authenticate(user, password));
	}
	
	public void killSession(String ticket)
	throws AuthenticationException, UnexpectedException, InterruptedException
	{
	    m_sessionManager.killSession(this.authenticate(ticket));
	}	
	
	public ServerSession login(String ticket)
	throws AuthenticationException, LoginProhibitedException, AlreadyLoggedInException, UnexpectedException
	{
	    // Log us in!
	    //
	    DataAccess da = DataAccessPool.instance().getDataAccess();
	    try
	    {
	        long id = this.authenticate(ticket);
	        ServerSession session = this.innerLogin(id, da);
	        this.consumeTicket(ticket);
	        return session;
	    }
	    finally
	    {
	        DataAccessPool.instance().returnDataAccess(da);
	    }
	}
	
	private ServerSession innerLogin(long id, DataAccess da)
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
			if(m_sessionManager.getSession(id) != null)
				throw new AlreadyLoggedInException();
			
			// Create a ServerSessionImpl, wrapped in a dynamic proxy and an InvocationHandler
			// keeping track of connections and transactions.
			//
			ServerSessionImpl session = new ServerSessionImpl(da, id, m_sessionManager);
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
	
	public ServerSession login(String user, String password)
	throws AuthenticationException, LoginProhibitedException, AlreadyLoggedInException, UnexpectedException
	{
		DataAccess da = DataAccessPool.instance().getDataAccess();
		try
		{
			UserManager um = da.getUserManager();
			 
			// Authenticate user
			//
			long id = um.authenticate(user, password);
			return this.innerLogin(id, da);
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
            m_validTickets.put(ticket, new Long(userId));
            m_ticketExpirations.schedule(new TicketKiller(ticket), ServerSettings.getTicketLifetime()); 
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
}
