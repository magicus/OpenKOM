/*
 * Created on Nov 9, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeUnread extends AbstractCommand
{
	public ChangeUnread(String fullName)
	{
		super(fullName);	
	}

	public void execute(Context context, String[] parameters)
		throws KOMException, IOException
	{
		if(parameters.length == 0)
			throw new UserException(context.getMessageFormatter().format("change.unread.no.parameter"));
		int n = 0;
		try
		{
			n = Integer.parseInt(parameters[0]);
		}
		catch(NumberFormatException e)
		{
			throw new UserException(context.getMessageFormatter().format("change.unread.invalid.number"));
		}
		context.getSession().changeUnread(n);
	}

	public boolean acceptsParameters()
	{
		return true;
	}
}
