/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SystemSettings 
{
	private final Properties m_resources;
	
    public SystemSettings(Map<String, String> parameters)
    {
        m_resources = new Properties();
        Set<String> keys = parameters.keySet();
        for (Iterator<String> iter = keys.iterator(); iter.hasNext();)
        {
            String key = iter.next();
            m_resources.setProperty(key, parameters.get(key));
        }
    }

    public String getString(String key)
	{
		return m_resources.getProperty(key);
	}
	
	public int getInt(String key)
	{
		return Integer.parseInt(this.getString(key));
	}
	
	public long getLong(String key)
	{
		return Long.parseLong(this.getString(key));
	}
	
}
