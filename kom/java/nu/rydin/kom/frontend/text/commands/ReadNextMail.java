/*
 * Created on Sep 20, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReadNextMail extends AbstractCommand
{
	public ReadNextMail(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}
	
	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
		context.getMessagePrinter().printMessage(context, 
		        context.getSession().readNextMessage(context.getLoggedInUserId()));
	}
}
