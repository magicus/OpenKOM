/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class DeleteMessage extends AbstractCommand 
{
	public DeleteMessage(String fullName) 
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		if (0 == parameters.length) 
		{
			throw new MissingArgumentException();
		}
		
		int n = -1;
		try
		{
			n = Integer.parseInt(parameters[0]);
		}
		catch (NumberFormatException e)
		{
			throw new BadParameterException();
		}
		context.getSession().deleteMessageInCurrentConference(n);

		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		out.println(fmt.format("delete.confirmation", 
			new Object [] { new Long(n) } ));	
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
