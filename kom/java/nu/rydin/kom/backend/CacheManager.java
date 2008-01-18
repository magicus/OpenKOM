/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend; 

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CacheManager
{
	private static CacheManager s_instance = new CacheManager();
	 
	private KOMCache m_userCache = new KOMCache(1000); // TODO: Read from config
	
	private KOMCache m_conferenceCache = new KOMCache(5000); // TODO: Read from config
	
	private KOMCache m_messageCache = new KOMCache(100); // TODO: Read from config
	
	private KOMCache m_nameCache = new KOMCache(1000); // TODO: Read from config
    
    private KOMCache m_permissionCache = new KOMCache(1000); // TODO: Read from config
	
	public static CacheManager instance()
	{
		return s_instance;
	}
	
	public void commit()
	{
		m_userCache.commit();
		m_conferenceCache.commit();
		m_messageCache.commit();
		m_nameCache.commit();
        m_permissionCache.commit();
	}
	
	public void rollback()
	{
		m_userCache.rollback();
		m_conferenceCache.rollback();
		m_messageCache.rollback();
		m_nameCache.rollback();
        m_permissionCache.rollback();
	}
	
	public void clear()
	{
	    m_userCache.clear();
		m_conferenceCache.clear();
		m_messageCache.clear();
		m_nameCache.clear();
        m_permissionCache.clear();
	}
	 
	public KOMCache getUserCache()
	{
		return m_userCache;
	}
	
	public KOMCache getConferenceCache()
	{
		return m_conferenceCache;
	}
	
	public KOMCache getMessageCache()
	{
		return m_messageCache;
	}
	
	public KOMCache getNameCache()
	{
	    return m_nameCache;
	}
    
    public KOMCache getPermissionCache()
    {
        return m_permissionCache;
    }
}
