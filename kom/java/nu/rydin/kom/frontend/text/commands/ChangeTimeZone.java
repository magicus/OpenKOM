/*
 * Created on Aug 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import nu.rydin.kom.exceptions.InvalidChoiceException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeTimeZone extends AbstractCommand
{
    public ChangeTimeZone(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter("change.timezone.prompt", true) });
    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        KOMWriter out = context.getOut();
        LineEditor in = context.getIn();
        MessageFormatter formatter = context.getMessageFormatter();
        
        // Get timezone string
        //
        String tz = (String) parameters[0];
        if(tz.length() == 0)
            throw new OperationInterruptedException();
        
        // Try to match with existing timezones.
        //
		String[] matches = this.matchTimezone(tz);
        int top = matches.length;
        
        // No match? Error!
        //
        if(top == 0)
            throw new InvalidChoiceException();
        
        // Resolve ambiguities (if any)
        //
        String match = top == 1 ? matches[0] : this.resolveAmbiguities(context, matches);
        
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

	private String resolveAmbiguities(Context context, String[] matches)
	throws InvalidChoiceException, OperationInterruptedException, IOException, InterruptedException
	{
		List matchList = new ArrayList(matches.length);
		
		for (int i = 0; i < matches.length; i++) {
			matchList.add(matches[i]);
		}
		int selection = Parser.askForResolution(context, matchList, "change.timezone.pick.prompt", true, "change.timezone.ambiguous");
		return matches[selection];
	}
    
    private String[] matchTimezone(String key)
    {
        key = key.toUpperCase().replace(' ', '_');
        ArrayList list = new ArrayList();
        String[] timeZones = TimeZone.getAvailableIDs();
        int top = timeZones.length;
        for(int idx  = 0; idx < top; ++idx)
        {
            String each = timeZones[idx];
            String candidate = each.toUpperCase();
            if(candidate.indexOf(key) != -1)
                list.add(each);
            if(candidate.equals(key))
                return new String[] { each };
        }
        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }
}
