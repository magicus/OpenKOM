/*
 * Created on Sep 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class LoginProhibitedException extends KOMException
{
    public LoginProhibitedException()
    {
        super();
    }

    public LoginProhibitedException(String msg)
    {
        super(msg);
    }

    public LoginProhibitedException(Object[] msgArgs)
    {
        super(msgArgs);
    }

    public LoginProhibitedException(Throwable t)
    {
        super(t);
    }

    public LoginProhibitedException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
