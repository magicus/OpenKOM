/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class AllRecipientsNotReachedException extends KOMException 
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
