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
    private final PrintWriter m_writer;
    
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
    
    public void highlight() 
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
}
