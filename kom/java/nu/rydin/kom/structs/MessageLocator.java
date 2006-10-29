/*
 * Created on Oct 26, 2006
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author Pontus Rydin
 */
public class MessageLocator
{
    private long globalId;
    private long conference;
    private int localNum;
    
    public static final MessageLocator NO_MESSAGE = new MessageLocator(-1L); 
    
    public MessageLocator(long globalId, long conference, int localNum)
    {
        this.globalId = globalId;
        this.conference = conference;
        this.localNum = localNum;
    }
    
    public MessageLocator(long globalId)
    {
        this(globalId, -1, -1);
    }
    
    public MessageLocator(long conference, int localnum)
    {
        this(-1, conference, localnum);
    }
    
    public boolean isValid()
    {
        if(this == NO_MESSAGE)
            return false;
        return conference != -1 || localNum != -1 || globalId != -1; 
    }
    
    public long getConference()
    {
        return conference;
    }
    
    public long getGlobalId()
    {
        return globalId;
    }
    
    public int getLocalnum()
    {
        return localNum;
    }

    public void setConference(long conference)
    {
        this.conference = conference;
    }

    public void setGlobalId(long globalId)
    {
        this.globalId = globalId;
    }

    public void setLocalnum(int localNum)
    {
        this.localNum = localNum;
    }
}
