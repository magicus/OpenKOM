/*
 * Created on Nov 5, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NoCurrentMessageException extends UserException
{
	public NoCurrentMessageException()
	{
		super();
	}
	
	public NoCurrentMessageException(String fullname)
	{
			super(fullname);
	}
}
