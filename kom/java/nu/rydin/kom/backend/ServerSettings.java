/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.Map;

import nu.rydin.kom.SystemSettings;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ServerSettings 
{
	public static SystemSettings s_settings;
	
	public static void initialize(Map<String, String> parameters)
	{
	    s_settings = new SystemSettings(parameters);
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
	
	public static int getNumDataAccess()
	{
	    return s_settings.getInt("server.initial.data.access");
	}
	
	public static long getTicketLifetime()
	{
		return s_settings.getLong("server.ticket.lifetime");
	}
    
    public static long getIdleNotificationThreashold()
    {
        return s_settings.getLong("server.idle.notification.threashold");
    }

	
}
