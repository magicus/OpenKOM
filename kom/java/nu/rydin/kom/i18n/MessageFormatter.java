/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.i18n;

import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageFormatter
{
	private final ResourceBundle m_resource;
	
	private final Locale m_locale;
	
	private TimeZone m_timeZone = TimeZone.getDefault();
	
	public MessageFormatter(Locale locale)
	{
	    m_resource 	= ResourceBundle.getBundle("nu.rydin.kom.i18n.messages", locale);
	    m_locale 	= locale;
	}
	
	public void setTimeZone(TimeZone timeZone)
	{
	    m_timeZone = timeZone;
	}
		
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
	
	public String getStringOrNull(String key)
	{
		try
		{
			return m_resource.getString(key);
		}
		catch(MissingResourceException e)
		{
			return null;
		}
	}

	public MessageFormat getFormat(String key)
	{
		String fmt = m_resource.getString(key);
		MessageFormat mf = new MessageFormat(fmt != null
			? fmt
			: "Unknown resource: " + key, m_locale);
		Format[] subformats = mf.getFormats();
		for (int idx = 0; idx < subformats.length; idx++)
        {
            Format each = subformats[idx];
            if(each instanceof SimpleDateFormat)
                ((SimpleDateFormat) each).setTimeZone(m_timeZone);
        }
		return mf;
	}
}
