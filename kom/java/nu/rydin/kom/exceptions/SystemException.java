/*
 * Created on 2004-aug-21
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Magnus Ihse
 */
public class SystemException extends KOMException
{

    public SystemException()
    {
        super();
    }

    public SystemException(String msg)
    {
        super(msg);
    }

    public SystemException(Throwable t)
    {
        super(t);
    }

    public SystemException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
