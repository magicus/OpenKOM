/*
 * Created on Aug 27, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.fullscreen;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.EmptyMessageException;
import nu.rydin.kom.exceptions.EventDeliveredException;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KeystrokeTokenizer;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.constants.Keystrokes;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.structs.MessageLocator;

/**
 * @author Pontus Rydin
 */
public class QuoteEditor extends FullscreenEditor
{
    private static final int QUOTE_WIDTH = 76;
    private final FullscreenMessageEditor mainEditor;
    
    public QuoteEditor(Context context, MessageLocator replyTo, FullscreenMessageEditor mainEditor) 
    throws IOException, ObjectNotFoundException, AuthorizationException, UnexpectedException, NoCurrentMessageException, EmptyMessageException
    {
        super(context);
        this.mainEditor = mainEditor;
        ServerSession session = context.getSession();
        String body = session.readMessage(replyTo).getMessage().getBody();
        WordWrapper ww = context.getWordWrapper(body, 
                QUOTE_WIDTH - 8); //8 = length of ">" + linenumber.
        Buffer buffer = this.getBuffer();
        
        // Let user pick lines to include
        //
        String line;
        while((line = ww.nextLine()) != null)
        {
            line = "> " + line;
            buffer.add(line);
        }
        if(buffer.size() == 0)
        	throw new EmptyMessageException();
    }

    public void mainloop() throws InterruptedException,
            OperationInterruptedException, IOException
    {
        Buffer buffer = this.getBuffer();
        DisplayController dc = this.getDisplayController();
        dc.messageBody();
        LineEditor in = this.getIn();
        in.pushTokenizer(this.getKeystrokeTokenizer());
        try
        {
	        // Set up some stuff
	        //
	        PrintWriter out = this.getOut();
	
	        // Go home
	        //
	        m_tc.setCursor(0, 0);
	
	        // Mainloop
	        // 
	        for (;;)
	        {
	            try
	            {
			        m_tc.setCursor(m_cy, 0);
			        String line = buffer.get(m_cy + m_viewportStart).toString(); 
	                out.print(line);
	                m_tc.eraseToEndOfLine();
	                out.flush();
	                KeystrokeTokenizer.Token token = in.readToken(0);
	                switch (token.getKind() & ~Keystrokes.TOKEN_MOFIDIER_BREAK)
	                {
	                case Keystrokes.TOKEN_UP:
	                    this.moveUp();
	                    break;
	                case Keystrokes.TOKEN_DOWN:
	                    this.moveDown();
	                    break;
	                case Keystrokes.TOKEN_CR:
	                    mainEditor.addQuote(line);
	                    this.moveDown();
	                    break;
	                case Keystrokes.TOKEN_ABORT:
	                    return;
	                }
	            } 
	            catch (EventDeliveredException e)
	            {
	                // Should not happen
	            }
	        }
	    }
        finally
        {
            in.popTokenizer();
        }
    }
}