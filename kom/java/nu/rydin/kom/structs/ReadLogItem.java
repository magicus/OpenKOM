/*
 * Created on Sep 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * Used for keeping a linked list of messages being read.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReadLogItem
{
    private final ReadLogItem previous; 
    
    private final long conference;
    
    private final int localNum;
    
    public ReadLogItem(ReadLogItem previous, long conference, int localNum)
    {
        this.previous 	= previous;
        this.conference = conference;
        this.localNum 	= localNum;
    }
    
    public long getConference()
    {
        return conference;
    }
    
    public int getLocalNum()
    {
        return localNum;
    }
    
    public ReadLogItem getPrevious()
    {
        return previous;
    }
    
    public String externalizeToString()
    {
        StringBuffer sb = new StringBuffer(50);
        sb.append(this.conference);
        sb.append(':');
        sb.append(this.localNum);
        return sb.toString();
    }
}
