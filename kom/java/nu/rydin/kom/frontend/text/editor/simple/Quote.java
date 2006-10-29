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
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Quote extends AbstractCommand
{
    private final int QUOTE_WIDTH = 76;
    
	public Quote(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
	}

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        // Check if this is a reply
        //
        EditorContext ec = (EditorContext) context;
        MessageLocator replyTo = ec.getReplyTo();
        if(!replyTo.isValid())
            throw new NotAReplyException();
        
        // Load original message
        //
        ServerSession session = context.getSession();
        String body = session.readMessage(replyTo).getMessage().getBody();
        WordWrapper ww = context.getWordWrapper(body, 
                QUOTE_WIDTH - 8); //8 = length of ">" + linenumber.
        LineEditor in = context.getIn();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        char yesChar = formatter.format("misc.y").toUpperCase().charAt(0);
        char quitChar = formatter.format("misc.q").toUpperCase().charAt(0);
        char goaheadChar = formatter.format("misc.goahead").toUpperCase().charAt(0);
        Buffer buffer = ec.getBuffer();
        
        // Let user pick lines to include
        //
        String line;
        boolean goahead = false;
        while((line = ww.nextLine()) != null)
        {
            try
            {
                line = "> " + line;
	            out.print(line);
	            out.flush();
	            char ch = goahead ? yesChar : Character.toUpperCase(in.readCharacter(0));
	            out.println();
	            if(ch == yesChar)
	                buffer.add(line + "\n");
	            else if(ch == goaheadChar)
	            {
	                buffer.add(line + "\n");
	                goahead = true;
	            }
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
