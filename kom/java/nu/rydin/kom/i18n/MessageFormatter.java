/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageFormatter
{
	private final ResourceBundle m_resource = 
		ResourceBundle.getBundle("nu.rydin.kom.i18n.messages");
		
	public String format(String key)
	{
		return this.format(key, new Object[0]);
	}
	
	public String format(String key, Object arg)
	{
		return this.format(key, new Object[] { arg });
	}
	
	public String format(String key, int arg)
	{
		return this.format(key, new Object[] { new Integer(arg) });
	}
	
	public String format(String key, long arg)
	{
		return this.format(key, new Object[] { new Long(arg) });
	}	

	public String format(String key, float arg)
	{
		return this.format(key, new Object[] { new Float(arg) });
	}
	
	public String format(String key, Object[] args)
	{
		return this.getFormat(key).format(args);
	}

	public MessageFormat getFormat(String key)
	{
		String fmt = m_resource.getString(key);
		return new MessageFormat(fmt != null
			? fmt
			: "Unknown resource: " + key);
	}
}
