/*
 * Created on Apr 12, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.sklaff;

import java.sql.Timestamp;

/**
 * @author Pontus Rydin
 */
public class ConfEntry
{
    private final String name;
    private final int owner;
    private final int type;
    private final Timestamp lastText;
    private final int sklaffId;
    private final int replyConf;
    private long openkomId;
    
    public ConfEntry(final String name, int owner, final int type,
            final Timestamp lastText, int replyConf, int sklaffId)
    {
        super();
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.lastText = lastText;
        this.replyConf = replyConf;
        this.sklaffId = sklaffId;
    }
    
    
    public Timestamp getLastText()
    {
        return lastText;
    }
    public String getName()
    {
        return name;
    }
    public int getOwner()
    {
        return owner;
    }
    public int getReplyConf()
    {
        return replyConf;
    }
    public long getOpenkomId()
    {
        return openkomId;
    }
    public int getSklaffId()
    {
        return sklaffId;
    }
    public int getType()
    {
        return type;
    }
   
    public void setOpenkomId(long id)
    {
        openkomId = id;
    }
}
