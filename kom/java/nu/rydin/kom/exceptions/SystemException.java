/*
 * Created on 2004-aug-21
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.exceptions;

/**
 * @author Magnus Ihse
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
