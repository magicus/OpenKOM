/*
 * Created on Aug 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 */
public class FileEditor extends AbstractEditor
{

    public FileEditor(Context context) throws IOException,
            UnexpectedException
    {
        super("/fileeditorcommands.list", context);
    }

    public UnstoredMessage edit(long replyTo)
            throws KOMException, InterruptedException, IOException
    {
        if (!this.mainloop(false))
            throw new OperationInterruptedException();
        return new UnstoredMessage("", m_context.getBuffer().toString());
    }
}