/*
 * Created on Oct 9, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.NamedObject;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class Status extends AbstractCommand
{
	public Status(String fullName)
	{
		super(fullName);	
	}
	
	public void execute(Context context, String[] parameters) 
	throws KOMException, IOException, InterruptedException
	{	

		// No parameters? That implies we're asking for our own status!
		//
		// Resolve name into id, possibly asking user to resolve
		// ambiguities.
		// 
		long id = parameters.length == 0
			? context.getLoggedInUserId()
			: NamePicker.resolveName(NameUtils.assembleName(parameters), (short) -1, context);

		// Call backend
		//		
		NamedObject no = context.getSession().getNamedObject(id);
		if(no instanceof ConferenceInfo)
			this.printConferenceStatus(context, (ConferenceInfo) no);
		else if(no instanceof UserInfo)
			this.printUserStatus(context, (UserInfo) no); 
		
	}
	
	protected void printUserStatus(Context context, UserInfo info)
	throws ObjectNotFoundException, UnexpectedException
	{
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		PrintUtils.printLabelled(out, formatter.format("status.user.userid"), 30, info.getUserid());
		PrintUtils.printLabelled(out, formatter.format("status.user.id"), 30, Long.toString(info.getId()));
		PrintUtils.printLabelled(out, formatter.format("status.user.name"), 30, info.getName());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.address1"), 
			30, info.getAddress1());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.address2"), 
			30, info.getAddress2());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.address3"), 
			30, info.getAddress3());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.address4"), 
			30, info.getAddress4());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.phone1"), 
			30, info.getPhoneno1());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.phone2"), 
			30, info.getPhoneno2());			
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.email1"), 
			30, info.getEmail1());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.email2"), 
			30, info.getEmail2());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.url"), 
			30, info.getUrl());
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.created"), 
				30, PrintUtils.printDate(info.getCreated()));
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.lastlogin"), 
				30, PrintUtils.printDate(info.getLastlogin()));
		out.println();
		
		// Has the user left a note?
		//
		try
		{
			out.print(context.getSession().readMagicMessage(MessageManager.ATTR_NOTE, info.getId()).getMessage().getBody());
			out.println();
		}
		catch (UnexpectedException e)
		{
			//
		}		
		
		// List memberships
		//
		out.println(formatter.format("status.user.memberof"));
		NameAssociation[] memberships = context.getSession().listMemberships(info.getId()); 
		int top = memberships.length;
		for(int idx = 0; idx < top; ++idx)
			out.println(memberships[idx].getName());
	}
	
	protected void printConferenceStatus(Context context, ConferenceInfo info)
	throws ObjectNotFoundException, UnexpectedException
	{
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		PrintUtils.printLabelled(out, formatter.format("status.conference.id"), 30, Long.toString(info.getId()));
		PrintUtils.printLabelled(out, formatter.format("status.conference.name"), 30, info.getName());
		PrintUtils.printLabelled(out, formatter.format("status.conference.admin"), 30, 
			context.getSession().getName(info.getAdministrator()));
		PrintUtils.printLabelled(out, formatter.format("status.conference.messages"), 30, 
					Integer.toString(info.getFirstMessage()) + " - " + 
					Integer.toString(info.getLastMessage()));			
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.created"), 
				30, PrintUtils.printDate(info.getCreated()));
		PrintUtils.printLabelledIfDefined(out, formatter.format("status.user.lasttext"), 
				30, PrintUtils.printDate(info.getLasttext()));
		
		// TODO: fulkod
		ListMembers l = new ListMembers("");
		String [] args = {info.getName()};
		try
		{
			l.execute(context, args);
		}
		catch (Exception e)
		{
			//
		}		
			
	}

	public boolean acceptsParameters()
	{
		return true;
	}	
}
