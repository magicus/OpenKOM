/*
 * Created on Nov 18, 2006
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.exceptions;

public class EmailRecipientNotRecognizedException extends SystemException 
{
    static final long serialVersionUID = 2005;
    
	public EmailRecipientNotRecognizedException(String email)
	{
		super(email);
	}
}
