/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NotLoggedInException extends SystemException
{
	public NotLoggedInException()
	{
		super();
	}
	
	public NotLoggedInException(String fullname)
	{
		super(fullname);
	}

}
