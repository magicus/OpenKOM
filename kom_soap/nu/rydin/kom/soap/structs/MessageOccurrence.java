/*
 * Created on Sep 29, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.soap.structs;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Pontus Rydin
 */
public class MessageOccurrence
{
	private long globalId; 
	private long conference;
	private int localnum;
	private Date timestamp;
	private short kind;
	private NameAssociation user;
		
    public MessageOccurrence()
    {
    }
    
    public MessageOccurrence(nu.rydin.kom.structs.MessageOccurrence nativeType)
    {
        this.globalId = nativeType.getGlobalId();
        this.conference = nativeType.getConference();
        this.localnum = nativeType.getLocalnum();
        this.timestamp = nativeType.getTimestamp();
        this.kind = nativeType.getKind();
        this.user = new NameAssociation(nativeType.getUser());
    }
    public long getGlobalId()
    {
        return globalId;
    }
    public void setGlobalId(long globalId)
    {
        this.globalId = globalId;
    }
    public short getKind()
    {
        return kind;
    }
    public void setKind(short kind)
    {
        this.kind = kind;
    }
    public int getLocalnum()
    {
        return localnum;
    }
    public void setLocalnum(int localnum)
    {
        this.localnum = localnum;
    }
    public Date getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }
    public NameAssociation getUser()
    {
        return user;
    }
    public void setUser(NameAssociation user)
    {
        this.user = user;
    }
    
    public nu.rydin.kom.structs.MessageOccurrence toNative()
    {
        return new nu.rydin.kom.structs.MessageOccurrence(
                globalId, 
                new Timestamp(timestamp.getTime()), 
                kind, 
                user.toNative(), 
                conference, 
                localnum);
    }
}
