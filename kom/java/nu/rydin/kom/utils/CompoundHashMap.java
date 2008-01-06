/*
 * Created on Jul 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */

package nu.rydin.kom.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class CompoundHashMap<K, V> 
{
    private HashMap<K, V> keys;
    private HashMap<V, K> values;

    public CompoundHashMap()
    {
    	keys = new HashMap<K, V>();
    	values = new HashMap<V, K>();
    }
    
    public void put(K key, V value)
    {
            this.removeByKey(key);
            this.removeByValue(value);
            keys.put(key, value);
            values.put(value, key);
    }
    
    public void putAll(Map<K, V> m)
    {
    	Iterator<K> it = m.keySet().iterator();
    	while (it.hasNext())
    	{
    		K key = it.next();
    		this.put (key, m.get(key));
    	}
    }

    public void removeByKey(K key)
    {
            V value = keys.get(key);
            keys.remove(key);
            values.remove(value);
    }

    public void removeByValue(V value)
    {
            K key = values.get(value);
            values.remove(value);
            keys.remove(key);
    }                                         
	
	public V getByKey(K key)
    {
            return keys.get(key);
    }

    public K getByValue(V value)
    {
            return values.get(value);
    }

    public boolean containsKey(K key)
    {
            return keys.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
            return values.containsKey(value);
    }

    public Set<K> keySet()
    {
            return keys.keySet();
    }

    public Set<V> valueSet()
    {
            return values.keySet();
    }


    public void clear()
    {
            keys.clear();
            values.clear();
    }
}                                                                         