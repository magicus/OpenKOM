/*
 * Created on Jun 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SkipTree extends AbstractCommand 
{
	public SkipTree (String fullname)
	{
		super(fullname, AbstractCommand.NO_PARAMETERS);
	}
	
 	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
 		ServerSession ss = context.getSession();
 		int n = ss.skipTree(ss.getLastMessageHeader().getId());
 		MessageFormatter mf = context.getMessageFormatter();
 		context.getOut().println (mf.format("skip.subject.message", n));
	}
}
