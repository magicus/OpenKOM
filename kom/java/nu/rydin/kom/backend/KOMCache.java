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

import com.frameworx.util.MRUCache;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KOMCache extends MRUCache
{
	private Set m_deferredInvalidations = new HashSet();
	
	private Map m_dirtyData = new HashMap();
	
	public KOMCache(int maxSize)
	{
		super(maxSize);
	}

	public KOMCache(int maxSize, EvictionPolicy evictionPolicy)
	{
		super(maxSize, evictionPolicy);
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
