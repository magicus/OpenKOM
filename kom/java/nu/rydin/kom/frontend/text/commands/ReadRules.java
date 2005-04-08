/*
 * Created on Jun 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.GenericException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
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
	public ReadRules (Context context, String fullname, long permissions)
	{
		super(fullname, new CommandLineParameter[] { new ConferenceParameter(false) }, permissions);
	}

	public void execute(Context context, Object[] parameterArray)
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
			throw new GenericException(formatter.format("read.message.not.found"));
		}
	}
}
