/*
 * Created on 2004-aug-21
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author Magnus Ihse Bursie
 */
public class SystemException extends KOMException
{
    static final long serialVersionUID = 2005;
   
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
