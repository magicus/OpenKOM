/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;

import nu.rydin.kom.SystemSettings;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ClientSettings
{
	public static final SystemSettings s_settings;
	
	static
	{
		try
		{
			s_settings = new SystemSettings("/client.properties");
		}
		catch(IOException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static int getTelnetPort()
	{
		return s_settings.getInt("telnet.server.port");
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
