/*
 * Created on Aug 25, 2004
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
public class FileStatus implements Serializable
{
    private final long m_parent;
    
    private final String m_name;
    
    private final int m_protection;
    
    private final Timestamp m_created;
    
    private final Timestamp m_updated;
    
    public FileStatus(long parent, String name, int protection, Timestamp created, 
            Timestamp updated)
    {
        m_parent 		= parent;
        m_name 			= name;
        m_protection	= protection;
        m_created 		= created;
        m_updated 		= updated;
    }
    
    public int getProtection()
    {
        return m_protection;
    }
    
    public Timestamp getCreated()
    {
        return m_created;
    }
    public String getName()
    {
        return m_name;
    }
    public long getParent()
    {
        return m_parent;
    }
    public Timestamp getUpdated()
    {
        return m_updated;
    }
}
