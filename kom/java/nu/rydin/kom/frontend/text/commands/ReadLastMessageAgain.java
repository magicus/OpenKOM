/*
 * Created on Jun 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.GenericException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UserException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.Envelope;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class ReadLastMessageAgain extends AbstractCommand 
{
	public ReadLastMessageAgain(Context context, String fullName, long permissions) 
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException, UserException 
	{
		try
		{
			Envelope env = context.getSession().readLastMessage();
			context.getMessagePrinter().printMessage(context, env);			
		}
		catch(ObjectNotFoundException e)
		{
			throw new GenericException(context.getMessageFormatter().format("read.message.not.found"));
		}
	}
}
