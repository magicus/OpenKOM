package nu.rydin.kom.structs;

import java.io.Serializable;
import java.util.Date;

public class SessionListItem implements Serializable
{
    private final int sessionId;
    private final short sessionType;
    private final long loginTime;
    private final long lastHeartbeat;
    public SessionListItem(int sessionId, short sessionType, long loginTime, long lastHeartbeat)
    {
        this.sessionId = sessionId;
        this.sessionType = sessionType;
        this.loginTime = loginTime;
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public long getLastHeartbeat()
    {
        return lastHeartbeat;
    }
    
    public long getLoginTime()
    {
        return loginTime;
    }
    
    public int getSessionId()
    {
        return sessionId;
    }
    
    public short getSessionType()
    {
        return sessionType;
    }
}
