/*
 * Created on Oct 19, 2003
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
public class ReadMessage extends AbstractCommand
{
	public ReadMessage(MessageFormatter formatter)
	{
		super(formatter);	
	}
	
	public void execute(Context context, String[] parameters) 
	throws KOMException, IOException
	{
		// Parse parameters
		//
		MessageFormatter formatter = context.getMessageFormatter();
		if(parameters.length == 0)
			throw new UserException(formatter.format("read.message.no.args"));
		int num = -1;
		try
		{
			num = Integer.parseInt(parameters[0]);
		}
		catch(NumberFormatException e)
		{
			throw new NumberFormatException(formatter.format("read.message.no.args"));
		}
		
	try
		{
			// Retreive message
			//
			Envelope env = context.getSession().readLocalMessage(num);
			
			// Print it using the default MessagePrinter
			//
			context.getMessagePrinter().printMessage(context, env);			
		}
		catch(ObjectNotFoundException e)
		{
			throw new UserException(formatter.format("read.message.not.found"));
		}
		
		
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
