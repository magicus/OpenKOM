/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UserListItem;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Who extends AbstractCommand
{

	public Who(MessageFormatter formatter)
	{
		super(formatter);	
	}
	
	public void execute(Context context, String[] parameters)
		throws KOMException, IOException, InterruptedException
	{
		MessageFormatter formatter = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		UserListItem[] users = context.getSession().listLoggedInUsers();
		int top = users.length;
		for(int idx = 0; idx < top; ++idx)
		{
			UserListItem each = users[idx];
			String confName = each.isInMailbox() 
				? formatter.format("misc.mailboxtitle")
				: each.getConferenceName();
			out.println(formatter.format("who.format", new Object[] { each.getUserName(), confName }));
		}
	}
}