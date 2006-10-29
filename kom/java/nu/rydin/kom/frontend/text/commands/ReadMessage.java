/*
 * Created on Oct 19, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.GenericException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReadMessage extends AbstractCommand
{
	public ReadMessage(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new TextNumberParameter(true), new ConferenceParameter(false)}, permissions);	
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
	    MessageLocator textNum = (MessageLocator) parameterArray[0];
	    if(parameterArray.length > 1 && parameterArray[1] != null)
            textNum.setConference(((NameAssociation) parameterArray[1]).getId());
		
		try
		{
			// Retreive message
			//
		    ServerSession session = context.getSession();
		    Envelope env = session.readMessage(textNum);
				
			// Print it using the default MessagePrinter
			//
			context.getMessagePrinter().printMessage(context, env);			
		}
		catch(ObjectNotFoundException e)
		{
			throw new GenericException(context.getMessageFormatter().format("read.message.not.found"));
		}		
	}
}
