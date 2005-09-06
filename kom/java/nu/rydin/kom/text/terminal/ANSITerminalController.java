/*
 * Created on Feb 6, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.text.terminal;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.AmbiguousPatternException;
import nu.rydin.kom.frontend.text.ANSIDisplayController;
import nu.rydin.kom.frontend.text.KeystrokeTokenizer;
import nu.rydin.kom.frontend.text.KeystrokeTokenizerDefinition;
import nu.rydin.kom.frontend.text.constants.Keystrokes;

/**
 * @author Pontus Rydin
 */
public class ANSITerminalController extends ANSIDisplayController implements TerminalController
{
    private static final KeystrokeTokenizerDefinition s_tokenizerDef;
	
	static
	{
	    try
	    {
		    s_tokenizerDef = new KeystrokeTokenizerDefinition(
			        new String[] { 
			                "\n",				// Newline
			                "\r",				// CR
			                "\u0008",			// BS
			                "\u007f",			// DEL
			                "\u0001",			// Ctrl-A
			                "\u0005",			// Ctrl-E
			                "\u0015",			// Ctrl-U
			                "\u0018",			// Ctrl-X
			                "\u0017",			// Ctrl-W
			                "\u001b[A",			// <esc> [ A
			                "\u001b[B", 		// <esc> [ B
			                "\u001b[C", 		// <esc> [ C
			                "\u0006", 			// Ctrl-F
			                "\u001b[D", 		// <esc> [ D
			                "\u0002", 			// Ctrl-B
			                "\u000c", 			// Ctrl-L
			                "\u0003", 			// Ctrl-C
			                "\u0010",			// Ctrl-P
			                "\u000e",			// Ctrl-N
			                "\u0004",			// Ctrl-D
			                "\u0019",			// Ctrl-Y
			                "\u001a"},			// Ctrl-Z			
			        new int[] {
			                Keystrokes.TOKEN_SKIP,				// Newline
			                Keystrokes.TOKEN_CR | 
		                		Keystrokes.TOKEN_MOFIDIER_BREAK,// CR
			                Keystrokes.TOKEN_BS,				// BS
			                Keystrokes.TOKEN_BS,				// DEL
			                Keystrokes.TOKEN_BOL,				// Ctrl-A
			                Keystrokes.TOKEN_EOL,				// Ctrl-E
			                Keystrokes.TOKEN_CLEAR_LINE | 
			                	Keystrokes.TOKEN_MOFIDIER_BREAK,// Ctrl-U
			                Keystrokes.TOKEN_CLEAR_LINE |
			                	Keystrokes.TOKEN_MOFIDIER_BREAK,// Ctrl-X
			                Keystrokes.TOKEN_DELETE_WORD, 		// Ctrl-W
			                Keystrokes.TOKEN_UP | 
			                	Keystrokes.TOKEN_MOFIDIER_BREAK,// <esc> [ A
			                Keystrokes.TOKEN_DOWN | 
			                	Keystrokes.TOKEN_MOFIDIER_BREAK,// <esc> [ B
			                Keystrokes.TOKEN_RIGHT,				// <esc> [ C
			                Keystrokes.TOKEN_RIGHT, 			// Ctrl-F
			                Keystrokes.TOKEN_LEFT, 				// <esc> [ D
			                Keystrokes.TOKEN_LEFT,				// Ctrl-B
			                Keystrokes.TOKEN_REFRESH |			
			                	Keystrokes.TOKEN_MOFIDIER_BREAK,// Ctrl-L
			                Keystrokes.TOKEN_ABORT |
			                	Keystrokes.TOKEN_MOFIDIER_BREAK,// Ctrl-C
			                Keystrokes.TOKEN_PREV,				// Ctrl-P
			                Keystrokes.TOKEN_NEXT,				// Ctrl-N
			                Keystrokes.TOKEN_DONE | 
	                			Keystrokes.TOKEN_MOFIDIER_BREAK,// Ctrl-D
				            Keystrokes.TOKEN_DELETE_LINE | 
		                		Keystrokes.TOKEN_MOFIDIER_BREAK,// Ctrl-y				                			
			                Keystrokes.TOKEN_DONE | 
		                		Keystrokes.TOKEN_MOFIDIER_BREAK});// Ctrl-Z			
	    }
	    catch(AmbiguousPatternException e)
	    {
	        throw new ExceptionInInitializerError(e);
	    }
	}
	
    public ANSITerminalController(PrintWriter out)
    {
        super(out);
    }

    public void startOfLine()
    {
        // Can't do this!
    }

    public void endOfLine()
    {
        // Can't do this!
    }

    public void top()
    {
        // Can't do this!
    }

    public void bottom()
    {
        // Can't do this!
    }
    
    public void up(int n)
    {
        this.printPreamble();
        m_writer.print(n);
        m_writer.print('A');
    }
    
    public void down(int n)
    {
        this.printPreamble();
        m_writer.print(n);
        m_writer.print('B');
    }

    public void forward(int n)
    {
        this.printPreamble();
        m_writer.print(n);
        m_writer.print('C');
    }

    public void backward(int n)
    {
        this.printPreamble();
        m_writer.print(n);
        m_writer.print('D');
    }

    public void setCursor(int line, int column)
    {
        this.printPreamble();
        m_writer.print(line + 1);
        m_writer.print(';');
        m_writer.print(column + 1);
        m_writer.print('H');
    }

    public void scrollUp(int lines)
    {
        this.printPreamble();
        m_writer.print('M');
    }

    public void scrollDown(int lines)
    {
        this.printPreamble();
        m_writer.print('D');
    }
    
    public void setScrollRegion(int start, int end)
    {
        // Can't do this
    }

    public void cancelScrollRegion()
    {
        // Can't do this
    }
    
    public void eraseToEndOfLine()
    {
        this.printPreamble();
        m_writer.print('K');
    }
    
    public void eraseLine()
    {
        this.printPreamble();
        m_writer.print("2K");        
    }
    
    public void eraseToStartOfLine()
    {
        this.printPreamble();
        m_writer.print("1K");        
    }
    
    public void eraseScreen()
    {
        this.printPreamble();
        m_writer.print("2J");
    }
    
    public void reverseVideo()
    {
        this.printPreamble();
        m_writer.print("7m");
    }
    
    public boolean canStartOfLine()
    {
        return false;
    }

    public boolean canEndOfLine()
    {
        return false;
    }

    public boolean canTop()
    {
        return false;
    }

    public boolean canBottom()
    {
        return false;
    }

    public boolean canForward()
    {
        return true;
    }

    public boolean canBackward()
    {
        return true;
    }

    public boolean canSetCursor()
    {
        return true;
    }

    public boolean canScrollUp()
    {
        return true;
    }

    public boolean canScrollDown()
    {
        return true;
    }

    public boolean canUp()
    {
        return true;
    }

    public boolean canDown()
    {
        return true;
    }
    
    public boolean canSetScrollRegion()
    {
        return false;
    }
    
    public boolean canEraseToEndOfLine()
    {
        return true;
    }
    
    public boolean canEraseLine()
    {
        return true;
    }
    
    public boolean canEraseToStartOfLine()
    {
        return true;
    }
    
    public boolean canEraseScreen()
    {
        return true;
    }
    
    protected void printPreamble()
    {
        m_writer.print("\u001b[");
    }    
    
    public KeystrokeTokenizer getKeystrokeTokenizer()
    {
        return s_tokenizerDef.createKeystrokeTokenizer();
    }
}
