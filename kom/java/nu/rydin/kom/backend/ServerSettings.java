/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.io.IOException;

import nu.rydin.kom.SystemSettings;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ServerSettings 
{
	public static final SystemSettings s_settings;
	
	static
	{
		try
		{
			s_settings = new SystemSettings("/server.properties");
		}
		catch(IOException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static String getJDBCDriverClass()
	{
		return s_settings.getString("server.jdbc.driver.class");
	}
	
	public static String getJDBCConnectString()
	{
		return s_settings.getString("server.jdbc.connect");
	}
	
	public static int getSessionShutdownRetries()
	{
		return s_settings.getInt("server.session.shutdown.retries");
	}
	
	public static long getSessionShutdownDelay()
	{
		return s_settings.getLong("server.session.shutdown.delay");
	}
	
}
