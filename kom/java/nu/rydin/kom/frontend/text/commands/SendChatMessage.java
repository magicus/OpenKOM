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
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.data.UserManager;
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
		
		// Special case: * sends to all
		//
		long user = "*".equals(parameters[0])
			? -1
			: NamePicker.resolveName(NameUtils.assembleName(parameters), UserManager.USER_KIND, context);

		// Set up
		//
		LineEditor in = context.getIn();
		PrintWriter out = context.getOut();
		ServerSession session = context.getSession();
		String me = session.getLoggedInUser().getName();
		
		// Print prompt
		//
		if (user == -1)
		{
		    //Special case, sending to all users
		    out.println(context.getMessageFormatter().format("chat.saytoall"));
		}
		else
		{
		    //Print username
		    out.println(context.getMessageFormatter().format("chat.saytouser", session.getName(user)));
		}
	    out.println("");
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
		if(user == -1)
			session.broadcastChatMessage(message);
		else
			session.sendChatMessage(user, message);
	}

	public boolean acceptsParameters()
	{
		return true;
	}
}
