/*
 * Created on Jun 13, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.exceptions.GenericException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ViewPresentation extends AbstractCommand 
{
	public ViewPresentation(Context context, String fullname)
	{
		super(fullname, new CommandLineParameter[] { new NamedObjectParameter(true)});
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
		try
		{
			long objectId = ((NameAssociation)parameterArray[0]).getId();
			context.getMessagePrinter().printMessage(context, context.getSession().
			        readTaggedMessage(MessageManager.ATTR_PRESENTATION, objectId));
		}
		catch (UnexpectedException e)
		{
			MessageFormatter formatter = context.getMessageFormatter();
			throw new GenericException(formatter.format("read.message.not.found"));
		}
	}
}
