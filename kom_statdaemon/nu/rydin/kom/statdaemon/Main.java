/*
 * Created on Sep 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.statdaemon;

import java.util.Locale;
import java.util.ResourceBundle;

import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author Pontus Rydin
 */
public class Main
{
    public static void main(String[] args)
    throws Exception
    {
        // Check if config filename was given on command line
        //
        int top = args.length;
        if(top > 1)
        {
            System.err.println("usage: Main [path to config file]");
            System.exit(1);
        }
        String config = top == 1 ? args[0] : "daemonconfig";
        
        // Load resource bundle
        //
        ResourceBundle properties = ResourceBundle.getBundle(config);
        
        // Load message formatter
        //
        MessageFormatter formatter = new MessageFormatter(Locale.getDefault(), "daemonmessages");
        
        // Call the acgtual code
        //
        StatisticsDaemon sd = new StatisticsDaemon(properties, formatter);
        sd.run();
    }
}
