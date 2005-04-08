/*
 * Created on Jun 10, 2004
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
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SkipSubject extends AbstractCommand 
{
	public SkipSubject (Context context, String fullname, long permissions)
	{
		super(fullname, AbstractCommand.NO_PARAMETERS, permissions);
	}
	
 	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
 		ServerSession ss = context.getSession();
 		int n = ss.skipMessagesBySubject(ss.getLastMessageHeader().getSubject(), false);
 		MessageFormatter mf = context.getMessageFormatter();
 		context.getOut().println (mf.format("skip.subject.message", n));
	}
}
