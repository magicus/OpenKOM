/*
 * Created on Aug 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.LineEditingInterruptedException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 */
public class FileEditor extends AbstractEditor
{

    public FileEditor(Context context) throws IOException,
            UnexpectedException
    {
        super("/fileeditorcommands.xml", context);
    }

	protected void refresh() throws KOMException
	{
	    new ShowSimpleFile(m_context, "").execute(m_context, new Object[0]);
	}
    
	//FIXME EDITREFACTOR: replyTo is completely irrelevant in this context.
    public UnstoredMessage edit(long replyTo)
            throws KOMException, InterruptedException, IOException
    {
        this.mainloop(false);
        return new UnstoredMessage("", m_context.getBuffer().toString());
    }
    
    protected void handleLineEditingInterruptedException(EditorContext context, LineEditingInterruptedException e)
    throws InterruptedException, OperationInterruptedException, IOException
    {
        // If user has written no more than three lines, abort immediately.
        if (context.getBuffer().size() <= 3)
        {
            throw e;
        }
        
        // Otherwise, ask user if he wants to abort.
	    MessageFormatter formatter = context.getMessageFormatter();
	    KOMWriter out = context.getOut();
	    LineEditor in = context.getIn();
	    out.print(formatter.format("simple.editor.abortfilequestion"));
	    out.flush();
	    String answer = in.readLine();
	    if (answer.equals(formatter.format("misc.y"))) {
	        throw e;
	    }
    }
}