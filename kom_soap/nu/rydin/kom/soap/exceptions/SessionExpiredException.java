/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.soap.exceptions;

import nu.rydin.kom.exceptions.KOMException;

/**
 * @author Pontus Rydin
 */
public class SessionExpiredException extends KOMException
{
    public SessionExpiredException()
    {
        super();
    }

    public SessionExpiredException(Object[] msgArgs)
    {
        super(msgArgs);
    }

    public SessionExpiredException(String msg)
    {
        super(msg);
    }

    public SessionExpiredException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public SessionExpiredException(Throwable t)
    {
        super(t);
    }
}
