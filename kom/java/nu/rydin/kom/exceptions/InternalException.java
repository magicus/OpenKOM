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

    /**
     * 
     */
    public InternalException()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     */
    public InternalException(String msg)
    {
        super(msg);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param t
     */
    public InternalException(Throwable t)
    {
        super(t);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param t
     */
    public InternalException(String msg, Throwable t)
    {
        super(msg, t);
        // TODO Auto-generated constructor stub
    }

}
