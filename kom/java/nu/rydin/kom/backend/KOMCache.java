/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.frameworx.util.MRUCache;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KOMCache extends MRUCache
{
	private Set m_deferredInvalidations = new HashSet();
	
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
		m_deferredInvalidations.add(key);
	}
	
	public synchronized void performDeferredInvalidations()
	{
		for(Iterator itor = m_deferredInvalidations.iterator(); itor.hasNext();)
			this.remove(itor.next());
	}
	
	public synchronized void discardDeferredInvalidations()
	{
		m_deferredInvalidations.clear();
	}
}
