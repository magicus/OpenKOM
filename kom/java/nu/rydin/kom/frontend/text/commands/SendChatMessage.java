/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.data.ObjectManager;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SendChatMessage extends AbstractCommand
{

	public SendChatMessage(String fullName)
	{
		super(fullName);	
	}

	public void execute(Context context, String[] parameters)
		throws KOMException, IOException, InterruptedException
	{
		// Handle parameters
		//
		if(parameters.length == 0)
			throw new MissingArgumentException();
		
		// Set up
		//
		LineEditor in = context.getIn();
		PrintWriter out = context.getOut();
		ServerSession session = context.getSession();
		String me = session.getLoggedInUser().getName();
				
		// Print prompt
		//
		out.print(me);
		out.print(": ");
		out.flush();
		
		
		// Read message
		//
		String message = in.readLine();
		
		// Empty message? User interrupted
		//
		if(message.length() == 0)
			return;

		
		// Send it
		//
		if("*".equals(parameters[0]))
			session.broadcastChatMessage(message);
		else
		{
			// TODO: Handle single recipients separately here (most common case and cheaper to do).

			// Now splice the parameters and tokenize them again.
			//
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < parameters.length; ++i)
			{
				sb.append(parameters[i]).append(" ");
			}
			String[] uarray = sb.toString().trim().split(",");
			
			// Resolve each name to an ID.
			//
			long[] destinations = new long[uarray.length];
			for (int i = 0; i < uarray.length; ++i)
			{
				try
				{
					destinations[i] = NamePicker.resolveName(uarray[i], ObjectManager.UNKNOWN_KIND, context);
				}
				catch (ObjectNotFoundException e)
				{
					destinations[i] = -1;
				}
			}
			
			// Finally call the multicast handler.
			//
			session.sendMulticastMessage(destinations, message);
		}
	}

	public boolean acceptsParameters()
	{
		return true;
	}
}
