/*
 * Created on Oct 5, 2003
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class DuplicateNameException extends KOMException
{
	public DuplicateNameException()
	{
		super();
	}
	public DuplicateNameException(String msg)
	{
		super(msg);
	}	
}
