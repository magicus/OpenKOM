/*
 * Created on Oct 15, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class UserException extends KOMException
{
    static final long serialVersionUID = 2005;
    
    public UserException()
    {
        super();
    }
    
	public UserException(String message)
	{
		super(message);		
	}

    public UserException(Object[] msgArgs)
    {
        super(msgArgs);
    }
}
