/*
 * Created on Jul 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.ansi.ANSISequences;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ANSIDisplayController implements DisplayController 
{
    private static final String HILITE_CHARS = "_";
    private static final short STATE_NORMAL 		= 0;
    private static final short STATE_READY_FOR_ATTR	= 1;
    private static final short STATE_AFTER_ATTR		= 2;
    
    protected final PrintWriter m_writer;
    
    public ANSIDisplayController(PrintWriter writer)
    {
        m_writer = writer;
    }
    
    public void prompt() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.YELLOW);
    }

    public void messageBody() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.WHITE);
    }

    public void messageSubject() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.WHITE);
    }
    
    public void quotedMessageBody()
    {
        m_writer.print(ANSISequences.RESET_ATTRIBUTES);
        m_writer.print(ANSISequences.WHITE);
    }
    
    public void chatMessageHeader() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.GREEN);
    }

    public void broadcastMessageHeader() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.CYAN);
    }

    public void chatMessageBody() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.WHITE);
    }

    public void broadcastMessageBody() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.WHITE);
    }
    
    public void input() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.WHITE);
    }

    public void output() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.WHITE);
    }
    
    public void quotedHighlight()
    {
        m_writer.print(ANSISequences.RESET_ATTRIBUTES);
        m_writer.print(ANSISequences.MAGENTA);
    }
    
    public void highlight() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.MAGENTA);
    }
    
    public void header() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.YELLOW);
    }    

    public void normal()
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.CYAN);    	
    }
    
    public void messageHeader() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.CYAN);
    }

    public void messageFooter() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.CYAN);
    }

    public void editorLineNumber() 
    {
        m_writer.print(ANSISequences.BRIGHT);
        m_writer.print(ANSISequences.CYAN);
    }

    public void reset()
    {
        m_writer.print(ANSISequences.RESET_ATTRIBUTES);
    }
    
    public void printWithAttributes(String s)
    {
        char attrChar = 0;
        int p = 0;
        boolean quoted = false;
        if(s.length() > 0 && s.charAt(0) == '>')
        {
            this.quotedMessageBody();
            quoted = true;
        }
        short state = STATE_READY_FOR_ATTR;
        for (int i = 0; i < s.length(); i++)
        {
            char ch = s.charAt(i);
            switch(state)
            {
            case STATE_NORMAL:
                m_writer.print(ch);
                if(Character.isWhitespace(ch))
                    state = STATE_READY_FOR_ATTR;
                break;
            case STATE_READY_FOR_ATTR:
                if(HILITE_CHARS.indexOf(ch) != -1)
                {
                    attrChar = ch;
                    p = i;
                    state = STATE_AFTER_ATTR;
                }
                else
                {
                    m_writer.print(ch);
                    if(!Character.isWhitespace(ch))
                        state = STATE_NORMAL;
                }
                break;
            case STATE_AFTER_ATTR:
                if(Character.isWhitespace(ch))
                {
                    // No whitespace allowed in hilited words
                    //
                    m_writer.print(s.substring(p, i));
                    attrChar = 0;
                }
                else if(ch == attrChar)
                {
                    // End of hilited word.
                    //
                    if(p + 1 == i)
                    {
	                    // Two attrchars in a row? Print one of them and move on
	                    //
                        m_writer.print(ch);
                    }
                    else
                    {
                        // Print with attributes
                        //
                        if(quoted)
                            this.quotedHighlight();
                        else
                            this.highlight();
                        m_writer.print(s.substring(p + 1, i));
                        if(quoted)
                            this.quotedMessageBody();
                        else
                            this.messageBody();
                    }
                    attrChar = 0;
                    state = STATE_NORMAL;
                    break;
                }
            }
        }
        if(attrChar != 0)
            m_writer.print(s.substring(p));
        this.messageBody();
    }
}
