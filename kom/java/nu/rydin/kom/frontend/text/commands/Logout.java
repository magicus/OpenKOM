/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Logout extends AbstractCommand
{
	public Logout(MessageFormatter formatter)
	{
		super(formatter);	
	}
	
	public void execute(Context ctx, String[] args)
	{
		// Not much to do here. The command loop is supposed to 
		// recognize us and ends its own misery when it sees us.
		//
	}
}
