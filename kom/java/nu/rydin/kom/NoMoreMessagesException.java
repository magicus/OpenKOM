/*
 * Created on Nov 6, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NoMoreMessagesException extends KOMException
{
	public NoMoreMessagesException()
	{
		super();
	}
	
	public NoMoreMessagesException(String fullname)
	{
			super(fullname);
	}

}
