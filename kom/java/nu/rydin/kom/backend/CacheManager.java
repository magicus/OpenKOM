/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CacheManager
{
	private static CacheManager s_instance = new CacheManager();
	 
	private KOMCache m_userCache = new KOMCache(50); // TODO: Read from config
	
	private KOMCache m_conferenceCache = new KOMCache(50); // TODO: Read from config
	
	private KOMCache m_messageCache = new KOMCache(50); // TODO: Read from config
	
	public static CacheManager instance()
	{
		return s_instance;
	}
	
	public void commit()
	{
		m_userCache.performDeferredInvalidations();
		m_conferenceCache.performDeferredInvalidations();
		m_messageCache.performDeferredInvalidations();
	}
	
	public void rollback()
	{
		m_userCache.discardDeferredInvalidations();
		m_conferenceCache.discardDeferredInvalidations();
		m_messageCache.discardDeferredInvalidations();
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
}
