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
public class MessageOccurrence extends MessageLocator implements Serializable
{
	private final Timestamp m_timestamp;
	private final short m_kind;
	private final NameAssociation m_user;
	
	public MessageOccurrence(long globalId, Timestamp timestamp, short kind, NameAssociation user, long conference, int localnum)
	{
        super(globalId, conference, localnum);
		m_timestamp 	= timestamp;
		m_kind 			= kind;
		m_user 			= user;
	}
	
	public short getKind()
	{
		return m_kind;
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
