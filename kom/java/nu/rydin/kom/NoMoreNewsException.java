/*
 * Created on Nov 5, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NoMoreNewsException extends KOMException
{
	public NoMoreNewsException()
	{
		super();
	}
	
	public NoMoreNewsException(String fullname)
	{
		super(fullname);
	}
}
