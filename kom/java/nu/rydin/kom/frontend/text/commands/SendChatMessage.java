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
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.editor.simple.SimpleChatEditor;
import nu.rydin.kom.frontend.text.editor.simple.AbstractEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

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
		DisplayController dc = context.getDisplayController();
		LineEditor in = context.getIn();
		PrintWriter out = context.getOut();
		ServerSession session = context.getSession();
		String me = session.getLoggedInUser().getName();
		MessageFormatter formatter = context.getMessageFormatter();

		//Parse parameters to get list of recipients.
		//
		long[] destinations = null;
		if("*".equals(parameters[0]))
		{
		    //Print beginning of prompt for message to all users.
			dc.genericHeader();
		    out.println(context.getMessageFormatter().format("chat.saytoall"));
		}
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
			destinations = new long[uarray.length];
			String recipients = "";
			for (int i = 0; i < uarray.length; ++i)
			{
			    long id = NamePicker.resolveName(uarray[i], NameManager.UNKNOWN_KIND, context);
				destinations[i] = id;
				recipients += ", " + session.getNamedObject(id).getName();
			}
			//TODO: If destinations is empty, maybe we should throw an exception here?
			
			//Print beginning of prompt for message to a list of users
			//
			if (recipients.length() > 2)
			{
			    recipients = recipients.substring(2);
			}
			dc.genericHeader();
		    out.println(context.getMessageFormatter().format("chat.saytouser", recipients));

		}
		out.flush();
		
		
		// Read message
		//
		AbstractEditor editor = new SimpleChatEditor(context.getMessageFormatter());
		String message = editor.edit(context, -1).getBody();
		// String message = in.readLine();
		
		// Empty message? User interrupted
		//
		if(message.length() == 0)
			return;

		
		// Send it
		//
		// Can't make it less ugly than this...
		//
		NameAssociation[] refused = "*".equals(parameters[0])
			? session.broadcastChatMessage(message)
			: session.sendMulticastMessage(destinations, message);
		
		// Print refused destinations (if any)
		//
		int top = refused.length;
		if(top > 0)
		{
		    // Build message
		    //
		    StringBuffer sb = new StringBuffer(200);
		    sb.append(formatter.format("chat.refused"));
		    for(int idx = 0; idx < top; ++idx)
		    {
		        sb.append(refused[idx].getName());
		        if(idx < top - 1)
		            sb.append(", ");
		    } 
		    
		    // Wordwrap it!
		    //
		    out.println();
		    WordWrapper ww = context.getWordWrapper(sb.toString());
		    String line = null;
		    while((line = ww.nextLine()) != null)
		        out.println(line);
		}
	}

	public boolean acceptsParameters()
	{
		return true;
	}
}
