/*
 * Created on Jun 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;


/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ReadRules extends AbstractCommand 
{
	public ReadRules (String fullname)
	{
		super(fullname, new CommandLineParameter[] { new ConferenceParameter(false) });
	}

	public void execute2(Context context, Object[] parameterArray)
	throws KOMException
	{
		try
		{
			if (parameterArray[0] == null)
			{
				context.getMessagePrinter().printMessage(context, context.getSession().getLastRulePosting());
			}
			else
			{
				context.getMessagePrinter().printMessage(context, context.getSession().getLastRulePostingInConference(((NameAssociation)parameterArray[0]).getId()));
			}
		}
		catch(ObjectNotFoundException e)
		{
			MessageFormatter formatter = context.getMessageFormatter();
			throw new UserException(formatter.format("read.message.not.found"));
		}
	}
}
