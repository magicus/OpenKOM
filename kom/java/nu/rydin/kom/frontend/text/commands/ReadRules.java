/*
 * Created on Jun 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;


/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ReadRules extends AbstractCommand 
{
	public ReadRules (String fullname)
	{
		super(fullname);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		try
		{
			if (0 == parameters.length)
			{
				context.getMessagePrinter().printMessage(context, context.getSession().getLastRulePosting());
			}
			else
			{
				context.getMessagePrinter().printMessage(context, context.getSession().getLastRulePostingInConference(NamePicker.resolveName(NameUtils.assembleName(parameters), ConferenceManager.CONFERENCE_KIND, context)));
			}
		}
		catch(ObjectNotFoundException e)
		{
			MessageFormatter formatter = new MessageFormatter();
			throw new UserException(formatter.format("read.message.not.found"));
		}
	}
}
