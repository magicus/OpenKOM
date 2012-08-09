/*
 * Created on Oct 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.UnexpectedException;

/**
 * @author Pontus Rydin
 */
public interface ServerSessionFactory
{
    /**
     * Lets the server know we received a successful login from a client. This gives 
     * us a chance to clear any intrusion detection counters we may have in place
     * for this client.
     * @param client The hostname/IP address of the client
     */
    public void notifySuccessfulLogin(String client);
    
    /**
     * Lets the server know we received a failed login from a client. This gives 
     * us a chance to increment any intrusion detection counters we may have in place
     * for this client. 
     * @param client The hostname/IP address of the client
     * @param limit The number of attempts allowed within the "lockout" time limit.
     * @param lockout The number of milliseconds from the last failed attempt a user is locked out
     * once the maximum number of failed attempts is reached.
     */
    public void notifyFailedLogin(String client, int limit, long lockout);
    
    public boolean isBlacklisted(String client);
    
    /**
     * Kills a session identified by a session if. Since we must allow this
     * before a user has logged in, the password is needed.
     * 
     * @param session The session id
     * @param user The login id
     * @param password The password
     * @throws AuthenticationException
     * @throws UnexpectedException
     * @throws InterruptedException
     */
    public void killSession(int sessionId, String user, String password)
    throws AuthenticationException, UnexpectedException, InterruptedException;

    /**
     * Kills a session identified by session id athenticated by a ticket.
     * 
     * @param session The session id
     * @param ticket The ticket
     * @throws AuthenticationException
     * @throws UnexpectedException
     * @throws InterruptedException
     */
	public void killSession(int session, String ticket)
	throws AuthenticationException, UnexpectedException, InterruptedException;
    
	/**
	 * Logs in a user and returns a <tt>ServerSession</tt> based on a ticket.
	 * 
	 * @param ticket The ticket
     * @param clientType The client type
     * @param allowMulti Do we allow multiple sessions per user?
	 * @return
	 * @throws AuthenticationException
	 * @throws LoginProhibitedException
	 * @throws AlreadyLoggedInException
	 * @throws UnexpectedException
	 */
    public ServerSession login(String ticket, short clientType, boolean allowMulti)
    throws AuthenticationException, LoginProhibitedException,
            AlreadyLoggedInException, UnexpectedException;

    /**
     * Logs in a user based on a username and a password.
     * 
     * @param user The login id
     * @param password The password
     * @param clientType The client type
     * @param allowMulti Do we allow multiple sessions per user?
     * @return A valid ServerSession
     * @throws AuthenticationException
     * @throws LoginProhibitedException
     * @throws AlreadyLoggedInException
     * @throws UnexpectedException
     */
    public ServerSession login(String user, String password, short clientType, boolean allowMulti)
    throws AuthenticationException, LoginProhibitedException,
            AlreadyLoggedInException, UnexpectedException;

    /**
     * Generates a login ticket based on a login id and a password.
     * 
     * @param user The login id
     * @param password The password
     * @return
     * @throws AuthenticationException
     * @throws UnexpectedException
     */
    public String generateTicket(String user, String password)
    throws AuthenticationException, UnexpectedException;

    /**
     * Discards a login ticket
     * 
     * @param ticket The ticket
     * @throws AuthenticationException
     */
	public void consumeTicket(String ticket)
	throws AuthenticationException;
    
	/**
	 * Checks validity of a login ticket.
	 * 
	 * @param ticket The ticket
	 * @return The id of the user
	 * @throws AuthenticationException
	 */
	public long authenticate(String ticket)
	throws AuthenticationException;
    
	/**
	 * Checks validity of a login id/password
	 * 
	 * @param user The login id
	 * @param password The password
	 * @return The id of the user
	 * @throws AuthenticationException
	 * @throws UnexpectedException
	 */
    public long authenticate(String user, String password)
    throws AuthenticationException, UnexpectedException;
    
    /**
     * Returns <tt>true</tt> if the system allows self-registration of users.
     * @return
     */
    public boolean allowsSelfRegistration()
    throws UnexpectedException;
    
    /**
     * Returns true if the specified login id exists. Note: This
     * function works only if self-registration is allowed.
     *  
     * @param userid The user id to check
     * @return
     * @throws AuthorizationException Self-registration is not allowed
     */
    public boolean loginExits(String userid)
    throws AuthorizationException, UnexpectedException;
    
    /**
     * Creates a self-registered user.
     *  
     * @param login The login id
     * @param password The password
     * @param fullName The full name
     * @param charset Name of character set to use
     * @return
     * @throws AuthorizationException Self-registration is not allowed
     * @throws UnexpectedException
     */
    public long selfRegister(String login, String password, String fullName, String charset)
    throws AuthorizationException, UnexpectedException, AmbiguousNameException, DuplicateNameException;
}