/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.constants.ChatRecipientStatus;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.editor.simple.AbstractEditor;
import nu.rydin.kom.frontend.text.editor.simple.SimpleChatEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SendChatMessage extends AbstractCommand
{

	public SendChatMessage(String fullName)
	{
		super(fullName, new CommandLineParameter[] { new RawParameter("chat.fulhack.raw.ask", true)});	
	}

	public void execute2(Context context, Object[] parameterArray)
		throws KOMException, IOException, InterruptedException
	{
	    String parameter = (String) parameterArray[0];
	    parameter = parameter.trim();
	    String[] parameters = parameter.split(" ");
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
			dc.normal();
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
			    long id = NamePicker.resolveNameToId(uarray[i], NameManager.UNKNOWN_KIND, context);
				destinations[i] = id;
				recipients += ", " + session.getName(id);
			}

			// Check that all users are logged in an can receive messages
			//
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			boolean error = false;
			int[] status = session.verifyChatRecipients(destinations);
			int top = status.length;
			for(int idx = 0; idx < top; ++idx)
			{
			    long each = destinations[idx];
			    switch(status[idx])
			    {
			    	case ChatRecipientStatus.NONEXISTENT:
			    	    pw.println(formatter.format("chat.nonexistent", session.getName(each)));
			    		error = true; 
			    		break;
			    	case ChatRecipientStatus.NOT_LOGGED_IN:
			    	    pw.println(formatter.format("chat.not.logged.in", session.getName(each)));
			    		error = true;
			    		break;
			    	case ChatRecipientStatus.REFUSES_MESSAGES:
			    	    pw.println(formatter.format("chat.refuses.messages", session.getName(each)));
			    		error = true;	
			    		break;			    	
			    	case ChatRecipientStatus.OK_CONFERENCE:
			    	case ChatRecipientStatus.OK_USER:
			    	    break;
			    	default:
			    	    throw new UnexpectedException(context.getLoggedInUserId());
			    }
			}
			if(error)
			    throw new UserException(sw.toString());
		
			//Print beginning of prompt for message to a list of users
			//
			if (recipients.length() > 2)
			{
			    recipients = recipients.substring(2);
			}
			dc.normal();
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
}
