/*
 * Created on Oct 2, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Henrik Schröder
 *
 */
public class RepliesNotAllowedException extends AuthorizationException
{
    public RepliesNotAllowedException()
    {
        super();
    }

    public RepliesNotAllowedException(String msg)
    {
        super(msg);
    }
    
    public RepliesNotAllowedException(Object[] msgArgs)
    {
        super(msgArgs);
    }
}
