package nu.rydin.kom.webui.beans;

import java.sql.Timestamp;

public class MessageHeaderBean
{
    private long globalId;
    
    private int localId;
    
    private long conference;
    
    private long authorId;
    
    private String authorName;
    
    private String subject;
    
    private Timestamp timestamp;

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }

    public long getAuthorId()
    {
        return authorId;
    }

    public void setAuthorId(long authorId)
    {
        this.authorId = authorId;
    }

    public String getAuthorName()
    {
        return authorName;
    }

    public void setAuthorName(String authorName)
    {
        this.authorName = authorName;
    }

    public long getConference()
    {
        return conference;
    }

    public void setConference(long conference)
    {
        this.conference = conference;
    }

    public long getGlobalId()
    {
        return globalId;
    }

    public void setGlobalId(long globalId)
    {
        this.globalId = globalId;
    }

    public int getLocalId()
    {
        return localId;
    }

    public void setLocalId(int localId)
    {
        this.localId = localId;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    
    
}
