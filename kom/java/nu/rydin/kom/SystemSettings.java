/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SystemSettings 
{
	private final PropertyResourceBundle m_resources;
	
	public SystemSettings(String resourceName)
	throws IOException
	{
		InputStream is = this.getClass().getResourceAsStream(resourceName);
		m_resources = new PropertyResourceBundle(is);
		is.close();
	}
	
	public String getString(String key)
	{
		return m_resources.getString(key);
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
