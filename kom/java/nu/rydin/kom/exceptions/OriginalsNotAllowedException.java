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
public class OriginalsNotAllowedException extends AuthorizationException
{
    public OriginalsNotAllowedException()
    {
        super();
    }

    public OriginalsNotAllowedException(String msg)
    {
        super(msg);
    }
    
    public OriginalsNotAllowedException(Object[] msgArgs)
    {
        super(msgArgs);
    }
}
