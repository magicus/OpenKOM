/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NotLoggedInException extends KOMException
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
