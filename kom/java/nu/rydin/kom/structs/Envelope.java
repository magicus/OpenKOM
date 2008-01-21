/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Envelope implements Serializable
{
    static final long serialVersionUID = 2005;
    
	public static class RelatedMessage implements Serializable
	{
        static final long serialVersionUID = 2005;
        
		private final MessageOccurrence m_occurrence;
		
		private final long m_author;
		
		private final Name m_authorName;
		
		private final long m_conference;
		
		private final Name m_conferenceName; 
		
		private final boolean m_local;
		
		public RelatedMessage(MessageOccurrence occurrence, long author, Name authorName, long conference, 
		        Name conferenceName, boolean local)
		{
			m_occurrence 	= occurrence;
			m_author		= author;
			m_authorName 	= authorName;
			m_conference	= conference;
			m_conferenceName= conferenceName;
			m_local 		= local;
		}
		
		public long getAuthor()
		{
		    return m_author;
		}

		public Name getAuthorName() 
		{
			return m_authorName;
		}
		
		public long getConference()
		{
		    return m_conference;
		}
		
		public Name getConferenceName()
		{
			return m_conferenceName;
		}

		public MessageOccurrence getOccurrence() 
		{
			return m_occurrence;
		}
		
		public boolean isLocal()
		{
			return m_local;
		}
	}
	
	private final Message m_message;
	
	private final MessageOccurrence m_primaryOccurrence;
	
	private final RelatedMessage m_replyTo;
	
	private final NameAssociation[] m_receivers;
	
	private final RelatedMessage[] m_replies;
	
	private final MessageOccurrence[] m_occurrences;
	
	private final MessageAttribute[] m_attributes;
	
	public Envelope(Message message, MessageOccurrence primaryOccurrence, RelatedMessage replyTo, 
		NameAssociation[] receivers, MessageOccurrence[] occurrences, MessageAttribute[] attributes, RelatedMessage[] replies)
	{
		m_message 			= message;
		m_primaryOccurrence = primaryOccurrence;
		m_replyTo 			= replyTo;
		m_receivers 		= receivers;
		m_replies 			= replies;		
		m_occurrences 		= occurrences;
		m_attributes		= attributes;
	}

	public Message getMessage()
	{
		return m_message;
	}

	public MessageOccurrence getPrimaryOccurrence()
	{
		return m_primaryOccurrence;
	}

	public NameAssociation[] getReceivers()
	{
		return m_receivers;
	}

	public RelatedMessage[] getReplies()
	{
		return m_replies;
	}
	
	public MessageOccurrence[] getOccurrences()
	{
		return m_occurrences;
	}

    public MessageAttribute[] getAttributes() 
    {
        return m_attributes;
    }
	
	public RelatedMessage getReplyTo()
	{
		return m_replyTo;
	}
}
