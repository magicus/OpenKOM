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
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SimpleChatEditor extends AbstractEditor 
{
    public SimpleChatEditor(MessageFormatter formatter)
            throws IOException, UnexpectedException 
    {
        super("/chateditorcommands.list", formatter);
    }

    public UnstoredMessage edit(Context underlying, long replyTo)
            throws KOMException, InterruptedException, IOException 
    {
        EditorContext context = new EditorContext(underlying);
		if(!this.mainloop(context, 
		        (context.getCachedUserInfo().getFlags1() & UserFlags.EMPTY_LINE_FINISHES_CHAT) != 0))
		    throw new OperationInterruptedException();
		return new UnstoredMessage("", context.getBuffer().toString());
    }
}
