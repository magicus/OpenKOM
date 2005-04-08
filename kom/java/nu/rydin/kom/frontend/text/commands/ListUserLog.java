/*
 * Created on Aug 24, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UserLogItem;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;
import nu.rydin.kom.utils.StringUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListUserLog extends AbstractCommand
{

	public ListUserLog(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(false) }, permissions);
	}

    public void execute(Context context, Object[] parameterArray)
    throws KOMException, IOException, InterruptedException
    {
        long user = parameterArray[0] != null
        	? ((NameAssociation) parameterArray[0]).getId()
        	: -1;
        Timestamp start = new Timestamp(0);
        Timestamp end = new Timestamp(System.currentTimeMillis() + 86400000L);
        ServerSession session = context.getSession();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        for(int offset = 0;;)
        {
            UserLogItem[] log = user == -1
            	? session.listUserLog(start, end, offset, 50)
            	: session.listUserLog(user, start, end, offset, 50);
            int top = log.length;
            if(top == 0)
                break;
            if(offset == 0)
            {
                // Print header
                //
                HeaderPrinter hp = new HeaderPrinter();
                hp.addHeader(formatter.format("list.user.log.login"), 17, false);
                hp.addHeader(formatter.format("list.user.log.time"), 6, true);
                hp.addHeader(formatter.format("list.user.log.read"), 5, true);
                hp.addHeader(formatter.format("list.user.log.posted"), 5, true);
                hp.addHeader(formatter.format("list.user.log.chat"), 5, true);
                hp.addHeader(formatter.format("list.user.log.broadcast"), 5, true);
                hp.addHeader(formatter.format("list.user.log.copied"), 5, true);
                hp.addSpace(1);
                hp.addHeader(formatter.format("list.user.log.name"), 10, false);
                hp.printOn(out);
            }
            offset += top;
            for(int idx = 0; idx < top; idx++)
            {
                UserLogItem each = log[idx];
                Calendar loginCal = Calendar.getInstance();
                loginCal.setTime(each.getLoggedIn());
                Calendar logoutCal = Calendar.getInstance();
                logoutCal.setTime(each.getLoggedOut());
                PrintUtils.printLeftJustified(out, context.smartFormatDate(each.getLoggedIn()), 17);
                PrintUtils.printRightJustified(out, StringUtils.formatElapsedTime(
                        logoutCal.getTimeInMillis() - loginCal.getTimeInMillis()), 6);
                PrintUtils.printRightJustified(out, Integer.toString(each.getNumRead()), 5);
                PrintUtils.printRightJustified(out, Integer.toString(each.getNumPosted()), 5);
                PrintUtils.printRightJustified(out, Integer.toString(each.getNumChats()), 5);
                PrintUtils.printRightJustified(out, Integer.toString(each.getNumBroadcasts()), 5);
                PrintUtils.printRightJustified(out, Integer.toString(each.getNumCopies()), 5);
                out.print(' ');
                out.println(NameUtils.stripSuffix(each.getUserName()));
            }
        }
    }
}
