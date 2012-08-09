package nu.rydin.kom.structs;

import java.util.Date;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class IntrusionAttempt
{
    private final String host;
    private final long firstAttempt;
    private long lastAttempt;
    private int count;
    private boolean isBlocked;
    private final int limit;
    private final long lockout;
    
    public long getLockout()
    {
        return lockout;
    }

    public IntrusionAttempt(String host, int limit, long lockout)
    {
        this.host = host;
        this.limit = limit;
        this.lockout = lockout;
        this.isBlocked = false;
        this.firstAttempt = System.currentTimeMillis();
        this.lastAttempt = this.firstAttempt;
        this.addAttempt();
    }
    
    public String getHost()
    {
        return host;
    }
    
    public long getFirstAttempt()
    {
        return firstAttempt;
    }
    
    public long getLastAttempt()
    {
        return lastAttempt;
    }
    
    public boolean isBlocked()
    {
        return isBlocked;
    }

    public void addAttempt()
    {
        ++this.count;
        Logger.debug(this, "Login failed for host: " + host + ". Number of attempts: " + count);
        lastAttempt = System.currentTimeMillis(); 
        if (limit <= this.count)
        {
            isBlocked = true;
            Logger.info(this, "Blacklisted host: " + host + " for " + (lockout / 1000) + " seconds");
        }
    }
    
    public int expireAttempt()
    {
        return --count;
    }
    
    // debug method
    //
    public int getCurrentCount()
    {
        return count;
    }
}
