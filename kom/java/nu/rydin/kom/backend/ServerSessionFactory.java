/*
 * Created on Oct 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.UnexpectedException;

/**
 * @author Pontus Rydin
 */
public interface ServerSessionFactory
{
    public abstract void killSession(String user, String password)
            throws AuthenticationException, UnexpectedException,
            InterruptedException;

    public abstract ServerSession login(String ticket)
            throws AuthenticationException, LoginProhibitedException,
            AlreadyLoggedInException, UnexpectedException;

    public abstract ServerSession login(String user, String password)
            throws AuthenticationException, LoginProhibitedException,
            AlreadyLoggedInException, UnexpectedException;

    public abstract String generateTicket(String user, String password)
            throws AuthenticationException, UnexpectedException;

    public abstract long authenticate(String user, String password)
            throws AuthenticationException, UnexpectedException;
}