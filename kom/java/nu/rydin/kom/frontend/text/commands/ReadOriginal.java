/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Envelope;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReadOriginal extends AbstractCommand
{
	public ReadOriginal(MessageFormatter formatter)
	{
		super(formatter);
	}

	public void execute(Context context, String[] parameters)
		throws KOMException, IOException
	{
		try
		{
			// Retreive message
			//
			Envelope env = context.getSession().readOriginalMessage();
				
			// Print it using the default MessagePrinter
			//
			context.getMessagePrinter().printMessage(context, env);			
		}
		catch(ObjectNotFoundException e)
		{
			throw new UserException(context.getMessageFormatter().format("read.message.not.found"));
		}
	}

}
