/*
 * Created on Jun 13, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.data.MessageManager;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ViewPresentation extends AbstractCommand 
{
	public ViewPresentation(String fullname)
	{
		super(fullname);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		if (0 == parameters.length)
		{
			throw new BadParameterException();
		}
		try
		{
			long objectId = NamePicker.resolveNameToId(NameUtils.assembleName(parameters), (short) -1, context);
			short kind = MessageManager.ATTR_PRESENTATION; 
			context.getMessagePrinter().printMessage(context, context.getSession().readMagicMessage(kind, objectId));
		}
		catch (UnexpectedException e)
		{
			MessageFormatter formatter = context.getMessageFormatter();
			throw new UserException(formatter.format("read.message.not.found"));
		}
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
