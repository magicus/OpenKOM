/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UserListItem;
import nu.rydin.kom.utils.PrintUtils;
import nu.rydin.kom.utils.StringUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Who extends AbstractCommand
{

	public Who(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);	
	}
	
    public void execute(Context context, Object[] parameterArray)
            throws KOMException
    {
		MessageFormatter formatter = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		UserListItem[] users = context.getSession().listLoggedInUsers();
		String loginLabel = formatter.format("who.login");
		String idleLabel = formatter.format("who.idle");
		String nameLabel = formatter.format("who.name");
		PrintUtils.printRightJustified(out, loginLabel, 6);
		PrintUtils.printRightJustified(out, idleLabel, 6);
		out.print(' ');
		out.println(nameLabel);
		PrintUtils.printRepeated(out, ' ', 6 - loginLabel.length());		
		PrintUtils.printRepeated(out, '-', loginLabel.length());
		PrintUtils.printRepeated(out, ' ', 6 - idleLabel.length());
		PrintUtils.printRepeated(out, '-', idleLabel.length());
		out.print(' ');
		PrintUtils.printRepeated(out, '-', nameLabel.length());
		out.println();		
		int top = users.length;
		for(int idx = 0; idx < top; ++idx)
		{
			UserListItem each = users[idx];
			String confName = each.isInMailbox() 
				? formatter.format("misc.mailboxtitle")
				: each.getConferenceName();
			long now = System.currentTimeMillis();
			PrintUtils.printRightJustified(out, StringUtils.formatElapsedTime(now - each.getLoginTime()), 6);
			long idle = now - each.getLastHeartbeat();
			PrintUtils.printRightJustified(out, idle >= 60000 ? StringUtils.formatElapsedTime(now - each.getLastHeartbeat()) : "", 6);
			out.print(' ');
			out.println(formatter.format("who.format", new Object[] { each.getUserName(), confName }));
		}
    }
}
