/*
 * Created on Oct 16, 2003
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
public class MessageOccurrence implements Serializable
{
	private final long m_globalId; 
	private final long m_conference;
	private final int m_localnum;
	private final Timestamp m_timestamp;
	private final short m_kind;
	private final NameAssociation m_user;
	
	public MessageOccurrence(long globalId, Timestamp timestamp, short kind, NameAssociation user, long conference, int localnum)
	{
		m_globalId		= globalId;
		m_timestamp 	= timestamp;
		m_kind 			= kind;
		m_user 			= user;
		m_conference 	= conference;
		m_localnum 		= localnum;
	}
	
	public long getGlobalId()
	{
		return m_globalId;
	}

	public long getConference()
	{
		return m_conference;
	}

	public short getKind()
	{
		return m_kind;
	}

	public int getLocalnum()
	{
		return m_localnum;
	}

	public Timestamp getTimestamp()
	{
		return m_timestamp;
	}

	public NameAssociation getUser()
	{
		return m_user;
	}	
}
