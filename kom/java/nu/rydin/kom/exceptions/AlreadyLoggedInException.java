/*
 * Created on Nov 12, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AlreadyLoggedInException extends SystemException
{
	public AlreadyLoggedInException()
	{
		super();
	}
	
	public AlreadyLoggedInException(String name)
	{
		super(name);
	}
}
