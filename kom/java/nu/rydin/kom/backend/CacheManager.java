/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import com.frameworx.util.MRUCache;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CacheManager
{
	private MRUCache m_userCache = new MRUCache(50); // TODO: Read from config
	
	private MRUCache m_conferenceCache = new MRUCache(50); // TODO: Read from config
	
	private MRUCache m_messageCache = new MRUCache(50); // TODO: Read from config
	 
	public MRUCache getUserCache()
	{
		return m_userCache;
	}
	
	public MRUCache getConferenceCache()
	{
		return m_conferenceCache;
	}
	
	public MRUCache getMessageCache()
	{
		return m_messageCache;
	}
}
