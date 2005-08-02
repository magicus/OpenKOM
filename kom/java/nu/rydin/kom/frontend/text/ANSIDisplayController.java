/*
 * Created on Jul 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;



import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.rydin.kom.frontend.text.ansi.ANSISequences;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ANSIDisplayController implements DisplayController 
{
    private static  Pattern PATTERN = Pattern.compile("(^_|.*?\\s_)([^\\s^_]+)(_\\s.*|_$)");
    private static final String HILITE_CHARS 		= "_";
    private static final short STATE_NORMAL 		= 0;
    private static final short STATE_READY_FOR_ATTR	= 1;
    private static final short STATE_AFTER_ATTR		= 2;
    private static final short STATE_AFTER_HILITE	= 3;
    
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
        boolean quoted = false;
        if(s.length() > 0 && s.charAt(0) == '>')
        {
            this.quotedMessageBody();
            quoted = true;
        }
        for(;;)
        {
            Matcher matcher = PATTERN.matcher(s);
            boolean processed = false;
	        while(matcher.find())
	        {
	            String preamble = matcher.group(1);
	            if(preamble == null)
	                continue;
	            m_writer.print(preamble.substring(0, preamble.length() - 1));
	            if(quoted)
	                this.quotedHighlight();
	            else
	                this.highlight();
	            m_writer.print(matcher.group(2));
	            if(quoted)
	                this.quotedMessageBody();
	            else
	                this.messageBody();            
	            s = matcher.group(3).substring(1);
	            processed = true;
	        }
	        if(!processed)
	        {
	            m_writer.print(s);
	            break;
	        }
        }
        if(quoted)
            this.messageBody();
    }
}
