package nu.rydin.kom.structs;

import java.util.Date;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class IntrusionAttempt
{
    private final String m_host;
    private final Date m_firstAttempt;
    private Date m_lastAttempt;
    private int m_count;
    private boolean m_isBlocked;
    private static int m_limit = 9;
    
    public IntrusionAttempt (String host)
    {
        Logger.debug(this, "New player: " + host);
        this.m_host = host;
        this.m_count = 1;
        this.m_isBlocked = false;
        this.m_firstAttempt = new Date();
        this.m_lastAttempt = new Date(m_firstAttempt.getTime());
    }
    
    public String getHost()
    {
        return m_host;
    }
    
    public Date getFirstAttempt()
    {
        return m_firstAttempt;
    }
    
    public Date getLastAttempt()
    {
        return m_lastAttempt;
    }
    
    public boolean isBlocked()
    {
        return m_isBlocked;
    }

    public void addAttempt()
    {
        ++this.m_count;
        m_lastAttempt.setTime(System.currentTimeMillis()); 
        if (m_limit <= this.m_count)
        {
            m_isBlocked = true;
        }
    }

    public static void setLimit (int limit)
    {
        m_limit = limit;
    }
    
    // debug method
    //
    public int getCurrentCount()
    {
        return m_count;
    }
}
