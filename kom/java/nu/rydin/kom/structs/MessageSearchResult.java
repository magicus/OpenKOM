/*
 * Created on Jul 15, 2004
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
public abstract class MessageSearchResult implements Serializable
{
    private final long m_globalid;
    private final Timestamp m_timestamp;
    protected final int m_localid;
    private final NameAssociation m_author;
    private final String m_subject;
    private final long m_replyTo;

    public MessageSearchResult(long globalid, int localid, NameAssociation author,
            String subject, long replyTo, Timestamp timestamp)
    {
        m_globalid 	= globalid;
        m_localid 	= localid;
        m_author 	= author;
        m_subject 	= subject;
        m_replyTo 	= replyTo;
        m_timestamp = timestamp;
    }

    public NameAssociation getAuthor()
    {
        return m_author;
    }
    public long getGlobalId()
    {
        return m_globalid;
    }

    public String getSubject()
    {
        return m_subject;
    }

    public int getLocalId()
    {
        return m_localid;
    }
    public long getReplyTo()
    {
        return m_replyTo;
    }
    public Timestamp getTimestamp()
    {
        return m_timestamp;
    }
}