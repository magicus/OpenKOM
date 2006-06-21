/*
 * Created on Dec 5, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UserListItem;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;
import nu.rydin.kom.utils.StringUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListSessions extends AbstractCommand
{

    public ListSessions(Context context, String fullName, long permissions)
    {
        super(fullName, AbstractCommand.NO_PARAMETERS, permissions);    
    }
    
    public void execute(Context context, Object[] parameterArray)
            throws KOMException
    {
        MessageFormatter formatter = context.getMessageFormatter();
        PrintWriter out = context.getOut();
        UserListItem[] users = context.getSession().listLoggedInUsers();
        DisplayController dc = context.getDisplayController();
        dc.normal();
        HeaderPrinter hp = new HeaderPrinter();
        hp.addHeader(formatter.format("list.sessions.session"), 7, true);
        hp.addHeader(formatter.format("list.sessions.login"), 7, true);
        hp.addHeader(formatter.format("list.sessions.idle"), 7, true);
        hp.addHeader(formatter.format("list.sessions.client"), 7, true);
        hp.addSpace(1);
        int termWidth = context.getTerminalSettings().getWidth();
        int firstColsWidth = 7 + 7 + 7 + 7 + 1;
        int lastColWidth = termWidth - firstColsWidth - 1 ; 
        hp.addHeader(formatter.format("list.sessions.name"), lastColWidth, false);
        hp.printOn(out);
        int top = users.length;
        dc.output();
        int active = 0;
        for(int idx = 0; idx < top; ++idx)
        {
            UserListItem each = users[idx];
            PrintUtils.printRightJustified(out, Integer.toString(each.getSessionId()), 7);
            long now = System.currentTimeMillis();
            PrintUtils.printRightJustified(out, StringUtils.formatElapsedTime(now - each.getLoginTime()), 7);
            long idle = now - each.getLastHeartbeat();
            PrintUtils.printRightJustified(out, idle >= 60000 ? StringUtils.formatElapsedTime(now - each.getLastHeartbeat()) : "", 7);
            out.print(' ');
            PrintUtils.printLeftJustified(out, formatter.format("clienttypes." + Integer.toString(each.getClientType())), 7);
            out.print(' ');
            PrintUtils.printIndented(out, context.formatObjectName(each.getUser()),
                    lastColWidth, 0, firstColsWidth);
            if(idle < 60000)
                ++active;
        }
        out.println();
        out.println(formatter.format("list.sessions.total", new Object[] { new Integer(top), new Integer(active) }));
    }
}
