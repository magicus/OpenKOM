/*
 * Created on Aug 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.util.TimeZone;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TimeZoneParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeTimeZone extends AbstractCommand
{
    public ChangeTimeZone(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new TimeZoneParameter(true) });    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        KOMWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        
        // Get timezone string
        //
        String match = TimeZoneParameter.getJavaNameForTimeZoneSelection((Integer) parameters[0]);
        
        // Update database and clear cache.
        //
        context.getSession().updateTimeZone(match);
        context.clearUserInfoCache();
        formatter.setTimeZone(TimeZone.getTimeZone(match));
        
        // Print confirmation
        //
        out.println();
        out.println(formatter.format("change.timezone.confirmation", match));
    }
}
