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
    private final NameAssociation m_conference;

    public GlobalMessageSearchResult(long globalid, int localid, NameAssociation conference, 
            NameAssociation author, String subject, long replyTo)
    {
        super(globalid, localid, author, subject, replyTo);
        m_conference = conference;
    }
    
    public NameAssociation getConference()
    {
        return m_conference;
    }
}
