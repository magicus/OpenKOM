/*
 * Created on Nov 3, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.events.SessionShutdownEvent;
import nu.rydin.kom.events.UserAttendanceEvent;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ServerSessionFactoryImpl 
{
	private Connection m_conn;
	
	private SessionManager m_sessionManager = new SessionManager();

	private static ServerSessionFactoryImpl s_instance = new ServerSessionFactoryImpl();
	
	public static ServerSessionFactoryImpl instance()
	{
		return s_instance;
	}
	
	protected ServerSessionFactoryImpl()
	{
	    // Make sure the DataAccess pool is initialized.
	    //
	    DataAccessPool.instance();
	}
	
	public void requestShutdown(String user, String password)
	throws AuthenticationException, UnexpectedException, InterruptedException
	{
		// Authenticate user
		//
		long id = this.authenticate(user, password);
		ServerSession session = m_sessionManager.getSession(id);
		
		// Not logged in? Nothing to shut down. Fail silently.
		//
		if(session == null)
			return;
			
		// Post shutdown event
		//
		session.postEvent(new SessionShutdownEvent());
		
		// Wait for session to terminate
		//
		int top = ServerSettings.getSessionShutdownRetries();
		long delay = ServerSettings.getSessionShutdownDelay();
		while(top-- > 0)
		{
			// Has it disappeared yet?
			//
			if(m_sessionManager.getSession(id) == null)
				return;
			Thread.sleep(delay);
		}
		
		// Bummer! The session did not shut down when we asked
		// it nicely. Mark it as invalid so that the next request
		// to the server is guaranteed to fail.
		//
		ServerSessionImpl ssi = (ServerSessionImpl) m_sessionManager.getSession(id);
		
		// Did it dissapear while we were fiddling around? 
		// Well... That's exactly what we want!
		// Note that it may also disappear while we're marking
		// it as invalid, but since that race-condition is completely
		// harmless, we don't waste time synchronizing.
		//
		if(ssi == null)
			return;
		ssi.markAsInvalid();
	}
	
	public ServerSession login(String user, String password)
	throws AuthenticationException, AlreadyLoggedInException, UnexpectedException
	{
		boolean committed = false;
		DataAccess da = DataAccessPool.instance().getDataAccess();
		try
		{
			UserManager um = da.getUserManager();
			 
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

			// Authenticate user
			//
			long id = um.authenticate(user, password);
			
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
			UserInfo ui = um.loadUser(id);
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
	
	protected long authenticate(String user, String password)
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
