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
public class InternalError extends Error
{
    public InternalError()
    {
        super();
    }

    public InternalError(String message)
    {
        super(message);
    }

    public InternalError(Throwable cause)
    {
        super(cause);
    }

    public InternalError(String message, Throwable cause)
    {
        super(message, cause);
    }
}
