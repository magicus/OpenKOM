/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nu.rydin.kom.structs.CacheInformation;

import com.frameworx.util.MRUCache;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KOMCache extends MRUCache
{
	private Set m_deferredInvalidations = new HashSet();
	
	private Map m_dirtyData = new HashMap();
	
	private long m_numAccesses = 0;
	
	private long m_numHits = 0;
	
	public KOMCache(int maxSize)
	{
		super(maxSize);
	}

	public KOMCache(int maxSize, EvictionPolicy evictionPolicy)
	{
		super(maxSize, evictionPolicy);
	}
	
	public Object get(Object key)
	{
	    Object answer = super.get(key);
	    ++m_numAccesses;
	    if(answer != null)
	        ++m_numHits;
	    return answer;
	}
	
	public long getNumAccesses()
	{
	    return m_numAccesses;
	}
	
	public long getNumHits()
	{
	    return m_numHits;
	}
	
	public CacheInformation getStatistics()
	{
	    return new CacheInformation(m_numAccesses, m_numHits);
	}
	
	public synchronized void registerInvalidation(Object key)
	{
		m_dirtyData.remove(key);
		m_deferredInvalidations.add(key);
	}
	
	public synchronized void deferredPut(Object key, Object value)
	{
		m_dirtyData.put(key, value);
	}
	
	public synchronized void performDeferredOperations()
	{
		for(Iterator itor = m_dirtyData.entrySet().iterator(); itor.hasNext();)
		{
			Map.Entry each = (Map.Entry) itor.next();
			this.put(each.getKey(), each.getValue());
		}		
		for(Iterator itor = m_deferredInvalidations.iterator(); itor.hasNext();)
			this.remove(itor.next());
	}
	
	public synchronized void discardDeferredInvalidations()
	{
		m_deferredInvalidations.clear();
	}
}
