/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.FlagParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ClearFlag extends AbstractCommand
{
	public ClearFlag(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new FlagParameter(true) } );
	}

	public void execute(Context context, Object[] parameterArray) throws KOMException, IOException, InterruptedException {
        Integer flagNumberInteger = (Integer) parameterArray[0];
        int flagNumber = flagNumberInteger.intValue();
        
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		
		long[] set = new long[UserFlags.NUM_FLAG_WORD];
		long[] reset = new long[UserFlags.NUM_FLAG_WORD];
		reset[flagNumber / 64] = 1 << (flagNumber % 64);
		context.getSession().changeUserFlags(set, reset);
		context.getOut().println(context.getMessageFormatter().format("clear.flag.confirmation", 
			context.getFlagLabels()[flagNumber]));

		// Clear cache
		//
		context.clearUserInfoCache();
    }
}
