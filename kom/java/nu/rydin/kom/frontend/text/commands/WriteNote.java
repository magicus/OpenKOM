/*
 * Created on Jun 13, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.constants.FileProtection;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class WriteNote extends AbstractCommand 
{
	public WriteNote(Context context, String fullname)
	{
		super(fullname, AbstractCommand.NO_PARAMETERS);
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
		UnstoredMessage msg = context.getMessageEditor().edit(-1);
		context.getSession().storeFile(context.getLoggedInUserId(), ".note.txt", msg.getBody(), 
		        FileProtection.ALLOW_READ);
	}
}
