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
 * @author Henrik Schröder
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class CompoundHashMap 
{
    private HashMap keys;
    private HashMap values;

    public CompoundHashMap()
    {
    	keys = new HashMap();
    	values = new HashMap();
    }
    
    public void put(Object key, Object value)
    {
            this.removeByKey(key);
            this.removeByValue(value);
            keys.put(key, value);
            values.put(value, key);
    }
    
    public void putAll(Map m)
    {
    	Iterator it = m.keySet().iterator();
    	while (it.hasNext())
    	{
    		String key = (String)it.next();
    		this.put (key, m.get(key));
    	}
    }

    public void removeByKey(Object key)
    {
            Object value = keys.get(key);
            keys.remove(key);
            values.remove(value);
    }

    public void removeByValue(Object value)
    {
            Object key = values.get(value);
            values.remove(value);
            keys.remove(key);
    }                                         
	
	public Object getByKey(Object key)
    {
            return keys.get(key);
    }

    public Object getByValue(Object value)
    {
            return values.get(value);
    }

    public boolean containsKey(Object key)
    {
            return keys.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
            return values.containsKey(value);
    }

    public Set keySet()
    {
            return keys.keySet();
    }

    public Set valueSet()
    {
            return values.keySet();
    }


    public void clear()
    {
            keys.clear();
            values.clear();
    }
}                                                                         