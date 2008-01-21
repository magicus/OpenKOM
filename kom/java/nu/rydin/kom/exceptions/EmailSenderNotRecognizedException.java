/*
 * Created on Nov 18, 2006
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.exceptions;

public class EmailSenderNotRecognizedException extends SystemException 
{
    static final long serialVersionUID = 2005;
    
	public EmailSenderNotRecognizedException(String email)
	{
		super(email);
	}
}
