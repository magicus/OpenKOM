/*
 * Created on Jun 26, 2004
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Henrik Schröder
 */
public class LocalMessageHeader extends MessageHeader implements Serializable 
{
    static final long serialVersionUID = 2005;
    
	private final long m_conference;
	private final int m_localnum;
	
	public LocalMessageHeader(long id, Timestamp created, long author, Name authorName, long replyTo, long thread, String subject, long conference, int localnum)
	{
		super(id, created, author, authorName, replyTo, thread, subject);
		m_conference = conference;
		m_localnum = localnum;
	}

	public long getConference() 
	{
		return m_conference;
	}

	public int getLocalnum() 
	{
		return m_localnum;
	}
}
