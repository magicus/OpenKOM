/*
 * Created on Jul 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.sql.Timestamp;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageLogItem 
{
    private final short m_kind;
    private final long m_author;
    private final String m_authorName;
    private final Timestamp m_created;
    private final String m_body;
    private final NameAssociation[] m_recipients;
    private final boolean m_sent;
    
    public MessageLogItem(short kind, long author, String authorName, Timestamp created, boolean sent, String body,  
            NameAssociation[] recipients)
    {
        m_kind			= kind;
        m_author 		= author;
        m_authorName	= authorName;
        m_created 		= created;
        m_body 			= body;
        m_sent			= sent;
        m_recipients	= recipients;
    }
    
    public short getKind()
    {
        return m_kind;
    }
    public long getAuthor() 
    {
        return m_author;
    }
    public String getAuthorName() 
    {
        return m_authorName;
    }

    public String getBody() 
    {
        return m_body;
    }

    public Timestamp getCreated() 
    {
        return m_created;
    }
    
    public boolean isSent()
    {
        return m_sent;
    }
    
    public NameAssociation[] getRecipients()
    {
        return m_recipients;
    }
}
