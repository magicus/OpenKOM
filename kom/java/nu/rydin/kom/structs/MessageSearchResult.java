/*
 * Created on Jul 15, 2004
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author Henrik Schröder
 */
public abstract class MessageSearchResult implements Serializable
{
    private final long m_globalid;
    protected final int m_localid;
    private final NameAssociation m_author;
    private final String m_subject;

    public MessageSearchResult(long globalid, int localid, NameAssociation author,
            String subject)
    {
        m_globalid = globalid;
        m_localid = localid;
        m_author = author;
        m_subject = subject;
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
}