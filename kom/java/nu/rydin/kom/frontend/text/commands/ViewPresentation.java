/*
 * Created on Jun 13, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.backend.data.MessageManager;
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
	public ViewPresentation(String fullname)
	{
		super(fullname, new CommandLineParameter[] { new NamedObjectParameter(true)});
	}

	public void execute2(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
		try
		{
			long objectId = ((NameAssociation)parameterArray[0]).getId();
			short kind = MessageManager.ATTR_PRESENTATION; 
			context.getMessagePrinter().printMessage(context, context.getSession().readMagicMessage(kind, objectId));
		}
		catch (UnexpectedException e)
		{
			MessageFormatter formatter = context.getMessageFormatter();
			throw new UserException(formatter.format("read.message.not.found"));
		}
	}
}
