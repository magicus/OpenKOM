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
import nu.rydin.kom.utils.Logger;

import com.frameworx.util.MRUCache;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KOMCache extends MRUCache
{
    private static class Entry
    {
        private final Object data;
        
        private long era;

        public Entry(Object data, long era)
        {
            super();
            this.data = data;
            this.era = era;
        }
    }
    
    private static class Transaction 
    {  
        private final Set<Object> deletions = new HashSet<Object>();
        
        private final Map<Object, Entry> dirtyData = new HashMap<Object, Entry>();
        
        private Transaction()
        {
        }
        
        public void put(Object key, Entry value)
        {
            dirtyData.put(key, value);
        }
        
        public void delete(Object key)
        {
            dirtyData.remove(key);
            deletions.add(key);
        }
        
        public Entry get(Object key)
        {
            return dirtyData.get(key);
        }
        
        public boolean pendingDeletion(Object key)
        {
            return deletions.contains(key);
        }
        
        public void commit(KOMCache cache)
        {
            for(Iterator itor = dirtyData.entrySet().iterator(); itor.hasNext();)
            {
                Map.Entry each = (Map.Entry) itor.next();
                Object key = each.getKey();
                Entry dirty = (Entry) each.getValue();
                Entry clean = (Entry) cache.get(key);
                
                // If dirty data isn't stale...
                //
                if(clean == null || dirty.era > clean.era)
                    cache.put(key, dirty);
            }       
            for(Iterator itor = deletions.iterator(); itor.hasNext();)
                cache.remove(itor.next());
            this.rollback();
        } 
        
        public void rollback()
        {
            dirtyData.clear();
            deletions.clear();
        }
    }
    
	private ThreadLocal<Transaction> transaction = new ThreadLocal<Transaction>()
	{
	    public Transaction initialValue()
	    {
	        return new Transaction();
	    }
	};
	
	private long era = 0;
		
	private long numAccesses = 0;
	
	private long numHits = 0;
	
	private boolean committing = false;
	
	public KOMCache(int maxSize)
	{
		super(maxSize);
	}

	public KOMCache(int maxSize, EvictionPolicy evictionPolicy)
	{
		super(maxSize, evictionPolicy);
	}
	
	public synchronized Object put(Object key, Object value)
	{
	    if(committing)
	        super.put(key, value);
	    else
	    {
	        Logger.warn(this, "You should use deferredPut instead!");
	        this.deferredPut(key, value);
	    }
	    return value;
	}
	
	public synchronized Object get(Object key)
	{
	    Object answer = this.innerGet(key);
	    ++numAccesses;
	    if(answer != null)
	        ++numHits;
	    return answer;
	}
	
	private synchronized Object innerGet(Object key)
	{
	    Transaction tx = transaction.get();
	    
	    // Pending deletion in this tx? No hit!
	    //
	    if(tx.pendingDeletion(key))
	        return null;
	    Entry entry = (Entry) super.get(key);
	    return entry != null ? entry.data : null;
	}
	
	public long getNumAccesses()
	{
	    return numAccesses;
	}
	
	public long getNumHits()
	{
	    return numHits;
	}
	
	public CacheInformation getStatistics()
	{
	    return new CacheInformation(numAccesses, numHits);
	}
	
	public synchronized void registerInvalidation(Object key)
	{
	    transaction.get().delete(key);
	}
	
	public synchronized void deferredPut(Object key, Object value)
	{
		transaction.get().put(key, new Entry(value, ++era));
	}
	
	public synchronized void commit()
	{
	    committing = true;
	    transaction.get().commit(this);
	    committing = false;
	}
	
	public synchronized void rollback()
	{
	    transaction.get().rollback();
	}
}
