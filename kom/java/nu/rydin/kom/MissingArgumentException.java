/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MissingArgumentException extends KOMException
{
	public MissingArgumentException()
	{
		super();
	}
	
	public MissingArgumentException(String fullname)
	{
		super(fullname);
	}
}
