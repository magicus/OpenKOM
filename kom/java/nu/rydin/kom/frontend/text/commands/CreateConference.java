/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.constants.Visibilities;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class CreateConference extends AbstractCommand
{
	public CreateConference(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new RawParameter("create.conference.param.0.ask", true) });
	}
	
	//FIXME Command should be rewritten using better parameter!
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException, DuplicateNameException
	{
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();
		String fullname = (String) parameterArray[0];
		
		if (fullname.equals(fmt.format("misc.mailboxtitle")))
		{
		    throw new DuplicateNameException();
		}

		// Do we have the permission to do this?
		//
		context.getSession().checkRights(UserPermissions.CREATE_CONFERENCE);
			
		// There must be a better way to do this..
		//
		boolean canDoMagic = false;
		boolean doMagic = false;
		short magicType = -1;
		try
		{
			context.getSession().checkRights(UserPermissions.CONFERENCE_ADMIN);
			canDoMagic = true;
		}
		catch (AuthorizationException e)
		{
			// Ignore, user simply doesn´t have conference_admin privileges. 
		}
		
		int choice = 0;

		// User may create magic conferences. Do it?
		//		
		String error = fmt.format("create.conference.invalid.choice"); 
		if (canDoMagic)
		{
			choice = in.getChoice(fmt.format("create.conference.magic") + " (" + fmt.format("misc.y") + "/" + fmt.format("misc.n") + ")? ", 
								  new String[] { fmt.format("misc.y"), fmt.format("misc.n") },
								  1, error);
			if (0 == choice)
			{
				doMagic = true;
				out.println(fmt.format("magic.conference.presentation.users"));
				out.println(fmt.format("magic.conference.presentation.conferences"));
				out.println(fmt.format("magic.conference.notes"));
				choice = in.getChoice(fmt.format("magic.conference.kind") + " (1/2/3)? ",
									  new String[] { "1", "2", "3" }, -1, error);
				
				if (-1 != choice)
				{
					magicType = (short)choice;
					long oldMagic = -1;
					try
					{
						oldMagic = context.getSession().getMagicConference((short)choice);
						choice = in.getChoice(fmt.format("magic.conference.exists", context.getSession().getName(oldMagic)) + "? ",
											  new String[] { fmt.format("misc.yes"), fmt.format("misc.no") },
											  1, error);
						if (0 != choice)
						{
							throw new OperationInterruptedException();
						}
					}
					catch (ObjectNotFoundException e)
					{
						// There was no previous magic conference of this kind. 
					}
				}
				else
				{
					doMagic = false;
				}
			}
		}
		
		short flags = 0;
		
		// Get conference type
		//
		choice = in.getChoice(fmt.format("create.conference.type"),
			new String[] 
			{
				fmt.format("create.conference.public"), 
				fmt.format("create.conference.exclusive"),
				fmt.format("create.conference.protected"),
			}, 0, error); 
		if(choice == 0)
			flags |= ConferencePermissions.READ_PERMISSION;
		short visibility = choice == 2 ? Visibilities.PROTECTED : Visibilities.PUBLIC;
			
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
		
		// Don't even ask if members don't have read permissions
		//
		int nonmemberFlags = 0;
		if((flags & ConferencePermissions.READ_PERMISSION) != 0)
		{
			// NONMEMBER PERMISSIONS
			//
			// Get permission to read
			//
			choice = in.getChoice(fmt.format("create.conference.nonmember.allowread"), 
				new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 0, error);
			if(choice == 0)
			    nonmemberFlags |= ConferencePermissions.READ_PERMISSION;
	
			// Get permission to write
			//
			choice = in.getChoice(fmt.format("create.conference.nonmember.allowwrite"), 
				new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 0, error);
			if(choice == 0)
			    nonmemberFlags |= ConferencePermissions.WRITE_PERMISSION;
				
			// Get permission to reply
			//
			choice = in.getChoice(fmt.format("create.conference.nonmember.allowreply"), 
				new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 0, error);
			if(choice == 0)
			    nonmemberFlags |= ConferencePermissions.REPLY_PERMISSION;
		}
			
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
					? NamePicker.resolveNameToId(line, NameManager.CONFERENCE_KIND, context)
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
			if (doMagic)
			{
				context.getSession().createMagicConference(fullname, flags, nonmemberFlags, visibility, replyConf, magicType);
			}
			else
			{
				context.getSession().createConference(fullname, flags, nonmemberFlags, visibility, replyConf);
			}
		}
		catch(DuplicateNameException e)
		{
			out.println(context.getMessageFormatter().format("create.conference.ambiguous", e.getMessage()));
		}
	}
}
