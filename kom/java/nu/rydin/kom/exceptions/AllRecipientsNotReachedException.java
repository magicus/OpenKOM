/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class AllRecipientsNotReachedException extends KOMSystemException 
{
	public AllRecipientsNotReachedException()
	{
		super();
	}

	public AllRecipientsNotReachedException (String fullname)
	{
		super(fullname);
	}
}
