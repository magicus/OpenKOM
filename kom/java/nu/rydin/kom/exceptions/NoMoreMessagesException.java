/*
 * Created on Nov 6, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NoMoreMessagesException extends KOMUserException
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
