/*
 * Created on Jun 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
//import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Envelope;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class ReadLastMessageAgain extends AbstractCommand 
{
	public ReadLastMessageAgain(String fullName) 
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException, UserException 
	{
		try
		{
			Envelope env = context.getSession().readLastMessage();
			context.getMessagePrinter().printMessage(context, env);			
		}
		catch(ObjectNotFoundException e)
		{
			throw new UserException(context.getMessageFormatter().format("read.message.not.found"));
		}
	}
}
