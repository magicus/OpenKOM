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
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListFlags extends AbstractCommand
{

	public ListFlags(String fullName)
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException
	{
		MessageFormatter formatter = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		String on = formatter.format("list.flags.on");
		String off = formatter.format("list.flags.off"); 
		long[] flags = context.getCachedUserInfo().getFlags();
		String[] flagLabels = context.getFlagLabels();
		for(int idx = 0; idx < UserFlags.NUM_FLAGS; ++idx)
		{
			if(flagLabels[idx] == null)
				continue;
			int flagWord = idx / 64;
			int flagMask = 1 << (idx % 64);
			out.print((flags[flagWord] & flagMask) == flagMask ? on : off);
			out.print("  ");
			out.println(flagLabels[idx]); 
		}
	}
}
