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
public class DummyDisplayController implements DisplayController 
{
    private final PrintWriter m_writer;
    
    public DummyDisplayController(PrintWriter writer)
    {
        m_writer = writer;
    }
    
    public void prompt() 
    {
    }

    public void normal()
    {
    }
    
    public void messageBody() 
    {
    }
    
    public void quotedMessageBody()
    {
    }

    public void messageSubject() 
    {
    }
    
    public void chatMessageHeader() 
    {
    }

    public void broadcastMessageHeader() 
    {
    }

    public void chatMessageBody() 
    {
    }

    public void broadcastMessageBody() 
    {
    }
    
    public void input() 
    {
    }

    public void output() 
    {
    }
    
    public void highlight() 
    {
    }

    public void messageHeader() 
    {
    }

    public void messageFooter() 
    {
    }

	public void editorLineNumber() 
	{
	}

    public void reset()
    {
        m_writer.print(ANSISequences.RESET_ATTRIBUTES);
    }
}
