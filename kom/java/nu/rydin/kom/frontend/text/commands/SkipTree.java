/*
 * Created on Jun 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.backend.ServerSession;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SkipTree extends AbstractCommand 
{
	public SkipTree (String fullname)
	{
		super(fullname);
	}
	
 	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
 		ServerSession ss = context.getSession();
 		int n = ss.skipTree(ss.getLastMessageHeader().getId());
 		MessageFormatter mf = new MessageFormatter();
 		context.getOut().println (mf.format("skip.subject.message", n));
	}
}
