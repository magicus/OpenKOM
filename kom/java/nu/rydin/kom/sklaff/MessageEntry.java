/*
 * Created on Apr 14, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.sklaff;

import java.sql.Timestamp;

/**
 * @author Pontus Rydin
 */
public class MessageEntry
{
    private final int num;
    private final int author;
    private final Timestamp created;
    private final int replyTo;
    private final int replyToConf;
    private final int replyToUser;
    private final int numLines;
    private final String subject;
    private final String body;
    
    public MessageEntry(final int num, final int author,
            final Timestamp created, final int replyTo, final int replyToConf, final int replyToUser,
            final int numLines, final String subject, String body)
    {
        super();
        this.num = num;
        this.author = author;
        this.created = created;
        this.replyTo = replyTo;
        this.replyToConf = replyToConf;
        this.replyToUser = replyToUser;
        this.numLines = numLines;
        this.subject = subject;
        this.body = body;
    }
        
    public int getAuthor()
    {
        return author;
    }
    public Timestamp getCreated()
    {
        return created;
    }
    public int getNum()
    {
        return num;
    }
    public int getNumLines()
    {
        return numLines;
    }
    public int getReplyTo()
    {
        return replyTo;
    }
    public int getReplyToConf()
    {
        return replyToConf;
    }    
    public int getReplyToUser()
    {
        return replyToUser;
    }
    public String getSubject()
    {
        return subject;
    }
    public String getBody()
    {
        return body;
    }
}
