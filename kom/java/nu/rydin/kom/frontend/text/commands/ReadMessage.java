/*
 * Created on Oct 19, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.KOMUserException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.TextNumber;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReadMessage extends AbstractCommand
{
	public ReadMessage(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new TextNumberParameter(true)});	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
	    TextNumber textNum = (TextNumber) parameterArray[0];
		int num = textNum.getNumber();
		
		try
		{
			// Retreive message
			//
		    ServerSession session = context.getSession();
			Envelope env = textNum.isGlobal()
				? session.readGlobalMessage(num)
				: session.readLocalMessage(num);
			
			// Print it using the default MessagePrinter
			//
			context.getMessagePrinter().printMessage(context, env);			
		}
		catch(ObjectNotFoundException e)
		{
			throw new KOMUserException(context.getMessageFormatter().format("read.message.not.found"));
		}		
	}
}
