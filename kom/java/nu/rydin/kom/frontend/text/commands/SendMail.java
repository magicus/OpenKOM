/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SendMail extends AbstractCommand
{
	public SendMail(String fullName)
	{
		super(fullName);	
	}

	public void execute(Context context, String[] parameters)
		throws KOMException, IOException, InterruptedException
	{
		// Resolve user parameter
		//
		if(parameters.length == 0)
			throw new MissingArgumentException();
		long user = NamePicker.resolveName(NameUtils.assembleName(parameters), (short) -1, context);
		
		// Get editor and execute it
		//
		UnstoredMessage msg = context.getMessageEditor().edit(context, -1);
		MessageOccurrence occ = context.getSession().storeMail(msg, user, -1);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
