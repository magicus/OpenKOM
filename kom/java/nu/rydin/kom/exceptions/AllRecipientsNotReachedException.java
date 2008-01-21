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
public class AllRecipientsNotReachedException extends SystemException 
{
    static final long serialVersionUID=2005;
    
	public AllRecipientsNotReachedException()
	{
		super();
	}

	public AllRecipientsNotReachedException (String fullname)
	{
		super(fullname);
	}
}
