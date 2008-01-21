/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.sql.Timestamp;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Message extends MessageHeader implements Serializable
{
    static final long serialVersionUID = 2005;
    
	private String m_body;
	
	private final MessageOccurrence[] m_occurrences;
	
	public Message(long id, Timestamp created, long author, Name authorName, long replyTo, long thread, String subject, String body,
		MessageOccurrence[] occurrences)
	{
		super(id, created, author, authorName, replyTo, thread, subject);
		m_body 			= body;
		m_occurrences 	= occurrences;
	}
	
	public String getBody()
	{
		return m_body;
	}
	
	public MessageOccurrence[] getOccurrences()
	{
		return m_occurrences;
	}
}
