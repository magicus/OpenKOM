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
public class KOMUserException extends KOMException
{
    public KOMUserException()
    {
        super();
    }
    
	public KOMUserException(String message)
	{
		super(message);		
	}

    public KOMUserException(Object[] msgArgs)
    {
        super(msgArgs);
    }
}
