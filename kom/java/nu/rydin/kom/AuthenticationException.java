/*
 * Created on Oct 5, 2003
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AuthenticationException extends KOMException
{
	public AuthenticationException()
	{
		super();
	}
	
	public AuthenticationException(String msg)
	{
		super(msg);
	}	
}
