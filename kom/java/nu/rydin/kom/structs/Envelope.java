/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Envelope implements Serializable
{
	public static class RelatedMessage implements Serializable
	{
		private final MessageOccurrence m_occurrence;
		
		private final String m_authorName;
		
		private final String m_conferenceName; 
		
		private final boolean m_local;
		
		public RelatedMessage(MessageOccurrence occurrence, String authorName, String conferenceName, boolean local)
		{
			m_occurrence 	= occurrence;
			m_authorName 	= authorName;
			m_conferenceName= conferenceName;
			m_local 		= local;
		}

		public String getAuthorName() 
		{
			return m_authorName;
		}
		
		public String getConferenceName()
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
	
	private final String[] m_receivers;
	
	private final RelatedMessage[] m_replies;
	
	private final MessageOccurrence[] m_occurrences;
	
	private final MessageAttribute[] m_attributes;
	
	public Envelope(Message message, MessageOccurrence primaryOccurrence, RelatedMessage replyTo, 
		String[] receivers, MessageOccurrence[] occurrences, MessageAttribute[] attributes, RelatedMessage[] replies)
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

	public String[] getReceivers()
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
