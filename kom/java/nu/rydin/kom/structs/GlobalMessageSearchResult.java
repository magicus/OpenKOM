/*
 * Created on Sep 12, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author Henrik
 */
public class GlobalMessageSearchResult extends MessageSearchResult
{
    private final long m_conferenceid;

    public GlobalMessageSearchResult(long globalid, int localid, long conferenceid, long authorid,
            Name authorname, String subject)
    {
        super(globalid, localid, authorid, authorname, subject);
        m_conferenceid = conferenceid;
    }
    
    public long getConferenceid()
    {
        return m_conferenceid;
    }
}
