/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UserListItem;
import nu.rydin.kom.utils.HeaderPrinter;
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
		HeaderPrinter hp = new HeaderPrinter();
		hp.addHeader(formatter.format("who.login"), 6, true);
		hp.addHeader(formatter.format("who.idle"), 6, true);
		hp.addSpace(1);
		hp.addHeader(formatter.format("who.name"), 20, false);
		hp.printOn(out);
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
