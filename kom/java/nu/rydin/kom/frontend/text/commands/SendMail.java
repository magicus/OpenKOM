/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SendMail extends AbstractCommand
{
	public SendMail(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(true) });	
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException, IOException, InterruptedException
	{
	    long user = ((NameAssociation)parameterArray[0]).getId();
		
		// Get editor and execute it
		//
		UnstoredMessage msg = context.getMessageEditor().edit(-1);
		MessageOccurrence occ = context.getSession().storeMail(msg, user, -1);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}	
}
