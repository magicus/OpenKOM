/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens. See www.gnu.org for details
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ObjectNotFoundException extends KOMException
{
	public ObjectNotFoundException()
	{
		super();
	}
	public ObjectNotFoundException(String msg)
	{
		super(msg);
	}
}
