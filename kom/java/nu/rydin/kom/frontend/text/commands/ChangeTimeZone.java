/*
 * Created on Aug 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TimeZone;

import nu.rydin.kom.InvalidChoiceException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeTimeZone extends AbstractCommand
{
    public ChangeTimeZone(String fullName)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter("change.timezone.prompt", true) });
    }

    public void execute2(Context context, Object[] parameters)
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
	throws InterruptedException, IOException, InvalidChoiceException
	{
        KOMWriter out = context.getOut();
        LineEditor in = context.getIn();
        MessageFormatter formatter = context.getMessageFormatter();
        int top = matches.length;
        for(int idx = 0; idx < top; ++idx)
        {
            out.print(idx + 1);
            out.print(". ");
            out.println(matches[idx]);
        }
        out.println();
        out.print(formatter.format("change.timezone.pick.prompt"));
        out.flush();
        String answer = in.readLine();
        try
        {
            return matches[Integer.parseInt(answer)];
        }
        catch(NumberFormatException e)
        {
            throw new InvalidChoiceException();
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            throw new InvalidChoiceException();
        }
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
