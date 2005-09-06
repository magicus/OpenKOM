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
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceType;
import nu.rydin.kom.utils.ConferenceUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class CreateConference extends AbstractCommand
{
	public CreateConference(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new RawParameter("create.conference.param.0.ask", true) }, permissions);
	}
	
    public void checkAccess(Context context) throws AuthorizationException
    {
        context.getSession().checkRights(UserPermissions.CREATE_CONFERENCE);
    }

    public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException, DuplicateNameException
	{
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();

		//FIXME This command only uses Parser to ask for conference name parameter.
		String fullname = (String) parameterArray[0];
		
		// Check name validity
		//
		context.checkName(fullname);
		
		// Get conference permissions and visibility
		//
		ConferenceType ct = ConferenceUtils.askForConferenceType(
		        context,
		        ConferencePermissions.NORMAL_PERMISSIONS,
		        ConferencePermissions.READ_PERMISSION,
		        Visibilities.PUBLIC);
			
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
			context.getSession().createConference(fullname, ct.getPermissions(), ct.getNonMemberPermissions(), ct.getVisibility(), replyConf);
		}
		catch(DuplicateNameException e)
		{
			out.println(context.getMessageFormatter().format("create.conference.ambiguous", e.getMessage()));
		}
	}
}
