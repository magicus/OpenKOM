/*
 * Created on Jul 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class DummyDisplayController implements DisplayController 
{   
    private PrintWriter m_writer; 
    
    public DummyDisplayController(PrintWriter writer)
    {
        m_writer = writer;
    }
    
    public void prompt() 
    {
        // We're a dummy, so we ignore everything
    }

    public void normal()
    {
        // We're a dummy, so we ignore everything
    }
    
    public void messageBody() 
    {
        // We're a dummy, so we ignore everything
    }
    
    public void quotedMessageBody()
    {
        // We're a dummy, so we ignore everything        
    }

    public void messageSubject() 
    {
        // We're a dummy, so we ignore everything        
    }
    
    public void chatMessageHeader() 
    {
        // We're a dummy, so we ignore everything        
    }

    public void broadcastMessageHeader() 
    {
        // We're a dummy, so we ignore everything        
    }

    public void chatMessageBody() 
    {
        // We're a dummy, so we ignore everything        
    }

    public void broadcastMessageBody() 
    {
        // We're a dummy, so we ignore everything        
    }
    
    public void input() 
    {
        // We're a dummy, so we ignore everything        
    }

    public void output() 
    {
        // We're a dummy, so we ignore everything        
    }
    
    public void header()
    {
        // We're a dummy, so we ignore everything
    }
    
    public void highlight() 
    {
        // We're a dummy, so we ignore everything        
    }
    
    public void quotedHighlight() 
    {
        // We're a dummy, so we ignore everything        
    }

    public void messageHeader() 
    {
        // We're a dummy, so we ignore everything        
    }

    public void messageFooter() 
    {
        // We're a dummy, so we ignore everything        
    }

	public void editorLineNumber() 
	{
        // We're a dummy, so we ignore everything	    
	}

    public void reset()
    {
        // We're a dummy, so we ignore everything
    }
    
    public void printWithAttributes(String s)
    {
        // We don't understand attributes, so we just print the string as is.
        //
        m_writer.print(s);
    }
}
