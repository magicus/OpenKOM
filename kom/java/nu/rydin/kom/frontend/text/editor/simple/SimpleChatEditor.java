/*
 * Created on Jul 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;

import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SimpleChatEditor extends AbstractEditor 
{
    public SimpleChatEditor(Context context)
            throws IOException, UnexpectedException 
    {
        super("/chateditorcommands.list", context);
    }

    public UnstoredMessage edit(long replyTo)
            throws KOMException, InterruptedException, IOException 
    {
		if(!this.mainloop(
		        (m_context.getCachedUserInfo().getFlags1() & UserFlags.EMPTY_LINE_FINISHES_CHAT) != 0))
		    throw new OperationInterruptedException();
		return new UnstoredMessage("", m_context.getBuffer().toString());
    }
}
