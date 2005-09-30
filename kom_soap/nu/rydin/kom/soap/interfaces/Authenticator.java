/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */

package nu.rydin.kom.soap.interfaces;

import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.soap.exceptions.SessionExpiredException;
import nu.rydin.kom.soap.structs.SecurityToken;

/**
 * @author Pontus Rydin
 */
public interface Authenticator
{
    /** 
     * Auhtenticates and returns a SecurityToken to be used for subsequent operations
     * 
     * @param username The username
     * @param password The password
     */
    public SecurityToken login(String username, String password)
    throws AuthenticationException, UnexpectedException, AlreadyLoggedInException, LoginProhibitedException;
    
    /**
     * Destroys an authentication token. The token will no longer be valid.
     * 
     * @param token The token to destroy.
     */
    public void discardToken(SecurityToken token)
    throws AuthenticationException, UnexpectedException, SessionExpiredException;
}
