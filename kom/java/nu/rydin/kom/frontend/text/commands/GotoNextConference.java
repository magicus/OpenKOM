/*
 * Created on Oct 26, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class GotoNextConference extends AbstractCommand
{
	public GotoNextConference(MessageFormatter formatter)
	{
		super(formatter);
	}
	
	public void execute(Context context, String[] parameters) 
	throws KOMException
	{
		long id = context.getSession().gotoNextConference();
		context.printCurrentConference();
	}

}