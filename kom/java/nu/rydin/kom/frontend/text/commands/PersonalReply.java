/*
 * Created on Jul 14, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class PersonalReply extends AbstractCommand
{
    public PersonalReply(String fullName)
    {
        super(fullName);
    }

	public void execute(Context context, String[] parameters) 
	throws KOMException, IOException, InterruptedException
	{
		// Check permissions
		//
		if(!context.getSession().hasPermissionInCurrentConference(ConferencePermissions.REPLY_PERMISSION))
			throw new AuthorizationException();

		// Parse parameters. No parameters means we're replying to the
		// last text read.
		//
		MessageFormatter formatter = context.getMessageFormatter();
		ServerSession session = context.getSession();
		long newMessageId = -1;
		MessageHeader mh = parameters.length == 0 
			? session.getMessageHeader(session.getCurrentMessage())
			: context.resolveMessageSpecifier(parameters[0]);
			
		// Get editor and execute it
		//
		UnstoredMessage msg = context.getMessageEditor().edit(context, mh.getId());
		
		// Store the message
		//
		MessageOccurrence occ = session.storeMail(msg, mh.getAuthor(), mh.getId());
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
