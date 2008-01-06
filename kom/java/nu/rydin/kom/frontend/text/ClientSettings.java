/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.util.Map;

import nu.rydin.kom.SystemSettings;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ClientSettings
{
	public static SystemSettings s_settings;
	
	public static void initialize(Map<String, String> parameters)
	{
	    s_settings = new SystemSettings(parameters);
	}
		
	public static int getEventPollInterval()
	{
		return s_settings.getInt("event.poll.interval");
	}

	public static String getCharsets()	
	{
		return s_settings.getString("supported.character.sets");
	}	
}
