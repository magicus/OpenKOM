/*
 * Created on Sep 3, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.EventDeliveredException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NotAReplyException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Quote extends AbstractCommand
{
	public Quote(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        // Check if this is a reply
        //
        EditorContext ec = (EditorContext) context;
        long replyTo = ec.getReplyTo();
        if(replyTo == -1)
            throw new NotAReplyException();
        
        // Load original message
        //
        ServerSession session = context.getSession();
        String body = session.readGlobalMessage(replyTo).getMessage().getBody();
        WordWrapper ww = context.getWordWrapper(body, 70); // TODO: Width?
        LineEditor in = context.getIn();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        char yesChar = formatter.format("misc.y").toUpperCase().charAt(0);
//        char noChar = formatter.format("misc.n").toUpperCase().charAt(0);
        char quitChar = formatter.format("misc.q").toUpperCase().charAt(0);
        Buffer buffer = ec.getBuffer();
        
        // Let user pick lines to include
        //
        String line;
        while((line = ww.nextLine()) != null)
        {
            try
            {
                line = "> " + line;
	            out.println(line);
	            char ch = Character.toUpperCase(in.readCharacter(0));
	            if(ch == yesChar)
	                buffer.add(line + "\n");
	            else if(ch == quitChar || ch == '\u0003')
	                break;
	            
	            // Otwherwise, just continue
	            //
            }
            catch(EventDeliveredException e)
            {
                // We don't handle events (right now)
                //
            }
        }
    }

}
