/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license. See www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ObjectNotFoundException extends UserException
{
	public ObjectNotFoundException()
	{
		super();
	}
	public ObjectNotFoundException(String msg)
	{
		super(new Object[] { msg });
	}
}
