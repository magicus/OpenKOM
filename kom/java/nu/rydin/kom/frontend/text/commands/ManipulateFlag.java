/*
 * Created on Jun 6, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.AmbiguousNameException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class ManipulateFlag extends AbstractCommand
{
	public ManipulateFlag(String fullName)
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
		throws KOMException, IOException, InterruptedException
	{
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		
		// Resolve flag name
		//
		if(parameters.length == 0)
			throw new MissingArgumentException();
		String[] flagLabels = context.getFlagLabels();
		String flagName = NameUtils.assembleName(parameters);
		int match = -1;
		int top = flagLabels.length;
		for(int idx = 0; idx < top; ++idx)
		{
			if(flagLabels[idx] != null && NameUtils.match(flagName, flagLabels[idx]))
			{
				// Ambigous?
				//
				if(match != -1)
					throw new AmbiguousNameException(flagName);
				match = idx;
			}
		}
		if(match == -1)
		{
			out.println(formatter.format("manipulate.flag.nonexistent", flagName));
			return;	
		}
		
		// Set/reset flags
		//
		this.manipulateFlag(context, match);
		
		// Clear cache
		//
		context.clearUserInfoCache();
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
	
	protected abstract void manipulateFlag(Context context, int idx)
	throws ObjectNotFoundException, UnexpectedException;
}
