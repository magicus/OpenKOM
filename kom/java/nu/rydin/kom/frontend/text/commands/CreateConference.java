/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.DuplicateNameException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.backend.data.UserManager;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CreateConference extends AbstractCommand
{
	public CreateConference(String fullName)
	{
		super(fullName);	
	}
	
	public void execute(Context context, String[] parameters) 
	throws KOMException, IOException, InterruptedException
	{
		// Do we have the permission to do this?
		//
		context.getSession().checkRights(UserPermissions.CREATE_CONFERENCE);
			
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();
		out.print(fmt.format("create.conference.fullname"));
		out.flush();
		String fullname = in.readLine();
		
		// Empty name? User interrupted
		//
		if(fullname.length() == 0)
			return;
		
		short flags = 0;
		
		// Get conference type
		//
		String error = fmt.format("create.conference.invalid.choice"); 
		int choice = in.getChoice(fmt.format("create.conference.type"),
			new String[] 
			{
				fmt.format("create.conference.public"), 
				fmt.format("create.conference.exclusive")
			}, 0, error); 
		if(choice == 0)
			flags |= ConferencePermissions.READ_PERMISSION;
			
		// Get permission to write
		//
		choice = in.getChoice(fmt.format("create.conference.allowwrite"), 
			new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 0, error);
		if(choice == 0)
			flags |= ConferencePermissions.WRITE_PERMISSION;
			
		// Get permission to reply
		//
		choice = in.getChoice(fmt.format("create.conference.allowreply"), 
			new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 0, error);
		if(choice == 0)
			flags |= ConferencePermissions.REPLY_PERMISSION;
			
		// Ask for reply conference
		//
		long replyConf = -1;
		for(;;)
		{
			try
			{
				out.print(fmt.format("create.conference.replyconference"));
				out.flush();
				String line = in.readLine();
				replyConf = line.length() != 0
					? NamePicker.resolveName(line, UserManager.USER_KIND, context)
					: -1; 
				break;
			}
			catch(ObjectNotFoundException e)
			{
				// Ask again...
				//
			}
		}
			
		// Go create it
		//			
		try
		{
			context.getSession().createConference(fullname, flags, NameManager.PUBLIC, replyConf);
		}
		catch(DuplicateNameException e)
		{
			out.println(context.getMessageFormatter().format("create.conference.ambiguous", e.getMessage()));
		}

	}

}
