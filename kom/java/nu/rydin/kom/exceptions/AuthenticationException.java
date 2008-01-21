/*
 * Created on Oct 5, 2003
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AuthenticationException extends SystemException
{
    static final long serialVersionUID = 2005;
    
	public AuthenticationException()
	{
		super();
	}
	
	public AuthenticationException(String msg)
	{
		super(msg);
	}	
}
