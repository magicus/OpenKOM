/*
 * Created on Oct 21, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.TextNumberParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class MarkTreeAsUnread extends AbstractCommand
{
	public MarkTreeAsUnread(Context context, String fullname, long permissions)
	{
        super(fullname, new CommandLineParameter[] { new TextNumberParameter(false) }, permissions);
	}
	
 	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
        MessageLocator ml;
 		ServerSession ss = context.getSession();
        if (null != parameterArray[0])
        {
            ml = (MessageLocator)parameterArray[0];
        }
        else
        {
            ml = new MessageLocator (ss.getLastMessageHeader().getId());
        }
        ml = ss.resolveLocator(ml);
       
 		int n = ss.markThreadAsUnread(ml.getGlobalId());
 		MessageFormatter mf = context.getMessageFormatter();
 		context.getOut().println (mf.format("mark.tree.as.unread.confirmation", n));
	}
}
