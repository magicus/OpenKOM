/*
 * Created on Aug 24, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class OutputInterruptedException extends KOMRuntimeException
{
    public OutputInterruptedException()
    {
        super();
    }

    public OutputInterruptedException(String msg)
    {
        super(msg);
    }

    public OutputInterruptedException(Throwable t)
    {
        super(t);
    }

    public OutputInterruptedException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
