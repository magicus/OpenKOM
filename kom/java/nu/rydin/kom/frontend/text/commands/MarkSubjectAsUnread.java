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
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author Pontus Rydin
 */
public class MarkSubjectAsUnread extends AbstractCommand
{
	public MarkSubjectAsUnread(Context context, String fullname)
	{
		super(fullname, AbstractCommand.NO_PARAMETERS);
	}
	
 	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
 		ServerSession ss = context.getSession();
 		int n = ss.markSubjectAsUnread(ss.getLastMessageHeader().getSubject(), true);
 		MessageFormatter mf = context.getMessageFormatter();
 		context.getOut().println (mf.format("mark.tree.as.unread.confirmation", n));
	}
}
