/*
 * Created on Aug 24, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserLogItem implements Serializable
{
    static final long serialVersionUID = 2005;
    
    private final long m_userId;
    
    private String m_userName;
    
    private Timestamp m_loggedIn;
    
    private Timestamp m_loggedOut;
    
    private int m_numPosted;
    
    private int m_numRead;
    
    private int m_numChats;
    
    private int m_numBroadcasts;
    
    private int m_numCopies;
    
    public UserLogItem(long userId)
    {
        m_userId = userId;
    }

    public UserLogItem(long userId, String userName, Timestamp loggedIn, Timestamp loggedOut, int numPosted, int numRead, int numChats, int numBroadcasts,
            int numCopies)
    {
        m_userId 		= userId;
        m_userName		= userName;
        m_loggedIn 		= loggedIn;
        m_loggedOut		= loggedOut;
        m_numPosted		= numPosted;
        m_numRead 		= numRead;
        m_numChats 		= numChats;
        m_numBroadcasts	= numBroadcasts;
        m_numCopies		= numCopies;
    }
    
    public String getUserName()
    {
        return m_userName;
    }
        
    public Timestamp getLoggedIn()
    {
        return m_loggedIn;
    }
    public void setLoggedIn(Timestamp loggedIn)
    {
        this.m_loggedIn = loggedIn;
    }
    public Timestamp getLoggedOut()
    {
        return m_loggedOut;
    }
    public void setLoggedOut(Timestamp loggedOut)
    {
        this.m_loggedOut = loggedOut;
    }
    public int getNumBroadcasts()
    {
        return m_numBroadcasts;
    }
    public void incNumBroadcasts()
    {
        ++this.m_numBroadcasts;
    }
    public int getNumChats()
    {
        return m_numChats;
    }
    public void incNumChats()
    {
        ++this.m_numChats;
    }
    public int getNumPosted()
    {
        return m_numPosted;
    }
    public void incNumPosted()
    {
        ++this.m_numPosted;
    }
    public int getNumRead()
    {
        return m_numRead;
    }
    public void incNumRead()
    {
        ++this.m_numRead;
    }
    public long getUserId()
    {
        return m_userId;
    }
    
    public int getNumCopies()
    {
        return m_numCopies;
    }
    
    public void incNumCopies()
    {
        ++m_numCopies;
    }
}
