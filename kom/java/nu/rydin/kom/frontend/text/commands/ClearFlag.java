/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ClearFlag extends ManipulateFlag
{

	public ClearFlag(String fullName)
	{
		super(fullName);
	}

	protected void manipulateFlag(Context context, int idx)
	throws ObjectNotFoundException, UnexpectedException
	{
		long[] set = new long[UserFlags.NUM_FLAG_WORD];
		long[] reset = new long[UserFlags.NUM_FLAG_WORD];
		reset[idx / 64] = 1 << (idx % 64);
		context.getSession().changeUserFlags(set, reset);
		context.getOut().println(context.getMessageFormatter().format("clear.flag.confirmation", 
			context.getFlagLabels()[idx]));
	}
}
