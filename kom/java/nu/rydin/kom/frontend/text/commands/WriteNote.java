/*
 * Created on Jun 13, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.data.MessageManager;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class WriteNote extends AbstractCommand 
{
	public WriteNote(String fullname)
	{
		super(fullname, AbstractCommand.NO_PARAMETERS);
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
		UnstoredMessage msg = context.getMessageEditor().edit(context, -1);
		MessageOccurrence occ = context.getSession().storeMagicMessage(msg, MessageManager.ATTR_NOTE, -1L);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
}
