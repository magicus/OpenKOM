/*
 * Created on Oct 15, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class UserException extends KOMException
{
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
