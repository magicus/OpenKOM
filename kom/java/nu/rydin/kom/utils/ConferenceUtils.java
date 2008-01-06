/*
 * Created on Sep 5, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.utils;

import java.io.IOException;

import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.Visibilities;
import nu.rydin.kom.exceptions.LineEditingDoneException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceType;

/**
 * @author Pontus Rydin
 */
public class ConferenceUtils
{
    public static ConferenceType askForConferenceType(Context context, int oldPermissions, int oldNonmemberpersmissions, 
            short visibility)
    throws LineEditingDoneException, OperationInterruptedException, IOException, InterruptedException
    {
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();
		
		int permissions = 0;
		int nonmemberpermissions = 0;

		int choice = 0;
		String error = fmt.format("create.conference.invalid.choice");

		// Get conference type
		//
		int type = (oldPermissions & ConferencePermissions.READ_PERMISSION) == 0 ? 1 : 0;
		if(visibility != Visibilities.PUBLIC)
		    type = 2;
		choice = in.getChoice(fmt.format("create.conference.type"),
			new String[] 
			{
				fmt.format("create.conference.public"), 
				fmt.format("create.conference.exclusive"),
				fmt.format("create.conference.protected"),
			}, type, error); 
		if(choice == 0)
			permissions |= ConferencePermissions.READ_PERMISSION;
		else 
		    permissions &= ~ConferencePermissions.READ_PERMISSION;
		visibility = choice == 2 ? Visibilities.PROTECTED : Visibilities.PUBLIC;
			
		// Get permission to write
		//
		choice = in.getChoice(fmt.format("create.conference.allowwrite"), 
			new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 
			(oldPermissions & ConferencePermissions.WRITE_PERMISSION) == 0 ? 1 : 0, error);
		if(choice == 0)
			permissions |= ConferencePermissions.WRITE_PERMISSION;
		else
		    permissions &= ~ConferencePermissions.WRITE_PERMISSION;
			
		// Get permission to reply
		//
		choice = in.getChoice(fmt.format("create.conference.allowreply"), 
			new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 
			(oldPermissions & ConferencePermissions.REPLY_PERMISSION) == 0 ? 1 : 0, error);
		if(choice == 0)
			permissions |= ConferencePermissions.REPLY_PERMISSION;
		else
		    permissions &= ~ConferencePermissions.REPLY_PERMISSION;
		
		// Don't even ask if members don't have read permissions
		//
		if((permissions & ConferencePermissions.READ_PERMISSION) != 0)
		{
			// NONMEMBER PERMISSIONS
			//
			// Get permission to read
			//
			choice = in.getChoice(fmt.format("create.conference.nonmember.allowread"), 
				new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 
				(oldNonmemberpersmissions & ConferencePermissions.READ_PERMISSION) == 0 ? 1 : 0, error);
			if(choice == 0)
			    nonmemberpermissions |= ConferencePermissions.READ_PERMISSION;
			else
			    nonmemberpermissions &= ~ConferencePermissions.READ_PERMISSION;
	
			// Get permission to write
			//
			choice = in.getChoice(fmt.format("create.conference.nonmember.allowwrite"), 
				new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 
				(oldNonmemberpersmissions & ConferencePermissions.WRITE_PERMISSION) == 0 ? 1 : 0, error);
			if(choice == 0)
			    nonmemberpermissions |= ConferencePermissions.WRITE_PERMISSION;
			else
			    nonmemberpermissions &= ~ConferencePermissions.WRITE_PERMISSION;
				
			// Get permission to reply
			//
			choice = in.getChoice(fmt.format("create.conference.nonmember.allowreply"), 
				new String[] { fmt.format("misc.yes"), fmt.format("misc.no")}, 
				(oldNonmemberpersmissions & ConferencePermissions.REPLY_PERMISSION) == 0 ? 1 : 0, error);
			if(choice == 0)
			    nonmemberpermissions |= ConferencePermissions.REPLY_PERMISSION;
			else
			    nonmemberpermissions &= ~ConferencePermissions.REPLY_PERMISSION;
		}
		return new ConferenceType(permissions, nonmemberpermissions, visibility);

    }
}
