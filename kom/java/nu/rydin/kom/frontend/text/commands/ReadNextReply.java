/*
 * Created on Oct 26, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReadNextReply extends AbstractCommand
{
	public ReadNextReply(String fullName)
	{
		super(fullName);
	}
	
	public void execute(Context context, String[] parameters) 
	throws KOMException, IOException
	{
		context.getMessagePrinter().printMessage(context, context.getSession().readNextReply());
	}
}
