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
    private final long m_authorid;
    private final Name m_authorname;
    private final String m_subject;

    public MessageSearchResult(long globalid, int localid, long authorid, Name authorname,
            String subject)
    {
        m_globalid = globalid;
        m_localid = localid;
        m_authorid = authorid;
        m_authorname = authorname;
        m_subject = subject;
    }

    public long getAuthorId()
    {
        return m_authorid;
    }

    public Name getAuthorName()
    {
        return m_authorname;
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