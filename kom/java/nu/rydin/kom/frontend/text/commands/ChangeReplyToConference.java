/*
 * Created on Oct 2, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Henrik Schröder
 *
 */
public class ChangeReplyToConference extends AbstractCommand
{
	public ChangeReplyToConference(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter("change.replyto.conference.param.0.ask", true), new ConferenceParameter(false) });
	}
	
    public void execute(Context context, Object[] parameterArray) 
    throws KOMException
	{
        // Get parameters
        //
        NameAssociation originalConference = (NameAssociation) parameterArray[0];
        NameAssociation replytoConference = (NameAssociation) parameterArray[1];
        

		// Check that we have administrator rights in the given conference
		//
		ServerSession session = context.getSession();
		session.assertConferencePermission(originalConference.getId(), ConferencePermissions.ADMIN_PERMISSION);

		// Do the change and print confirmation
		//
		if (replytoConference == null)
		{
		    session.changeReplyToConference(originalConference.getId(), -1L);
		    context.getOut().println(context.getMessageFormatter().format("change.replyto.conference.clear", new Object[] { originalConference.getName() }));
		}
		else
		{		    
		    session.changeReplyToConference(originalConference.getId(), replytoConference.getId());
		    context.getOut().println(context.getMessageFormatter().format("change.replyto.conference.confirmation", new Object[] { originalConference.getName(), replytoConference.getName() }));
		}
	}
}
