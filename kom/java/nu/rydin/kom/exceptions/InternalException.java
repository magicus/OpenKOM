/*
 * Created on Sep 17, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class InternalException extends SystemException
{

    public InternalException()
    {
        super();
    }

    public InternalException(String msg)
    {
        super(msg);
    }

    public InternalException(Throwable t)
    {
        super(t);
    }

    public InternalException(String msg, Throwable t)
    {
        super(msg, t);
    }

}
