/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.structs.*;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface MessageEditor
{
	public UnstoredMessage edit()
	throws KOMException, InterruptedException, IOException;
	
	public UnstoredMessage edit(long replyTo, long replyToLocal, long recipientId, 
	        Name recipientName, long replyToAuthor, Name replyToAuthorName, 
	        String oldSubject)
		throws KOMException, InterruptedException;	
	
	public NameAssociation getRecipient();
	
	public void setRecipient(NameAssociation recipient);
	
	public void setReplyTo(long replyTo);
}
