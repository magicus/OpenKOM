/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.FlagParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ClearFlag extends AbstractCommand
{
    public ClearFlag(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[]
        { new FlagParameter(true, context.getFlagLabels("userflags")) });
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {
        Integer flagNumberInteger = (Integer) parameterArray[0];
        int flagNumber = flagNumberInteger.intValue();

        long[] set = new long[UserFlags.NUM_FLAG_WORD];
        long[] reset = new long[UserFlags.NUM_FLAG_WORD];
        reset[flagNumber / 64] = 1 << (flagNumber % 64);
        context.getSession().changeUserFlags(set, reset);

        // Clear cache
        //
        context.clearUserInfoCache();
        // Only necessary if flag reset was ANSI...
        context.getDisplayController().reset();
        
        // Display confirmation
        //
        context.getOut().println(
                context.getMessageFormatter().format("clear.flag.confirmation",
                        context.getFlagLabels("userflags")[flagNumber]));
    }
}