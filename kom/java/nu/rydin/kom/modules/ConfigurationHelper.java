package nu.rydin.kom.modules;

import java.util.Map;

import nu.rydin.kom.utils.Logger;

public class ConfigurationHelper
{
    public static int getInt(Map<String, String> properties, String name, int defaultValue)
    {
        try
        {
            return Integer.parseInt(properties.get(name));
        }
        catch(NumberFormatException e)
        {
            Logger.warn(ConfigurationHelper.class, "Invalid or missing value for '" + name + "'. Using default value: " + defaultValue);
            return defaultValue;
        }
    }
    
    public static long getLong(Map<String, String> properties, String name, long defaultValue)
    {
        try
        {
            return Long.parseLong(properties.get(name));
        }
        catch(NumberFormatException e)
        {
            Logger.warn(ConfigurationHelper.class, "Invalid or missing value for '" + name + "'. Using default value: " + defaultValue);
            return defaultValue;
        }
    }

}
