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
public class MessageHeader implements Serializable
{
	private final long m_id;
	private final Timestamp m_created;	
	private final long m_author;
	private final Name m_authorName;
	private final long m_replyTo;
	private final String m_subject;
	
	public MessageHeader(long id, Timestamp created, long author, Name authorName, long replyTo, String subject)
	{
		m_id			= id;
		m_created 		= created;
		m_author 		= author;
		m_authorName	= authorName;
		m_replyTo		= replyTo;
		m_subject 		= subject;
	}
	
	public long getId()
	{
		return m_id;
	}
	
	public long getAuthor()
	{
		return m_author;
	}
	
	public Name getAuthorName()
	{
		return m_authorName;
	}	
	
	public long getReplyTo()
	{
		return m_replyTo;
	}

	public Timestamp getCreated()
	{
		return m_created;
	}

	public String getSubject()
	{
		return m_subject;
	}
}
