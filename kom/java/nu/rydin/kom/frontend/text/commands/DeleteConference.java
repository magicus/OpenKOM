/*
 * Created on Jun 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NoMoreNewsException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class DeleteConference extends AbstractCommand 
{
	public DeleteConference (Context context, String fullname)
	{
		super(fullname, new CommandLineParameter[] { new ConferenceParameter(true)});
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
	    
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
		long conference = nameAssociation.getId();
		ServerSession ss = context.getSession();

		// Do we have the right to do this?
	    //
	    if(!(ss.hasPermissionInConference(conference, ConferencePermissions.ADMIN_PERMISSION)
	       || context.getCachedUserInfo().hasRights(UserPermissions.CONFERENCE_ADMIN)))
	       throw new AuthorizationException();
		MessageFormatter mf = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		if (ss.isMagicConference(conference))
		{
			out.println(mf.format("delete.magic.warning"));
		}
		out.println(mf.format("delete.conference.warning"));
		int choice = in.getChoice(mf.format("delete.conference.confirm",
											ss.getName(conference)) +
				     			  	" (" +
									mf.format("misc.yes") +
									"/" + 
									mf.format("misc.no") +
									")? ",
			                      new String[] { mf.format("misc.no"), 
												 mf.format("misc.yes") },
								  0,
								  mf.format("nu.rydin.kom.exceptions.InvalidChoiceException.format"));
		if (1 == choice)
		{
		    long currentConf = context.getSession().getCurrentConferenceId();
			ss.deleteConference(conference);

			// Go to next conference if we just signed of
			// from the current one.
			//
			if(currentConf == conference)
			{
			    out.println();
			    try
			    {
			        ss.gotoNextConference();
			    }
			    catch(NoMoreNewsException e)
			    {
			        // No more messages! Go to the mailbox!
			        // 
			        ss.gotoConference(context.getLoggedInUserId());
			    }
			    context.printCurrentConference();
			}

		}
	}
}
