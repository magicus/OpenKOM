/*
 * Created on Jan 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.fullscreen;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.EscapeException;
import nu.rydin.kom.exceptions.EventDeliveredException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.KOMRuntimeException;
import nu.rydin.kom.exceptions.LineEditingDoneException;
import nu.rydin.kom.exceptions.LineOverflowException;
import nu.rydin.kom.exceptions.LineUnderflowException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.StopCharException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KeystrokeTokenizer;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.TerminalSettings;
import nu.rydin.kom.frontend.text.constants.Keystrokes;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.editor.simple.SaveEditorException;
import nu.rydin.kom.frontend.text.terminal.TerminalController;
import nu.rydin.kom.frontend.text.terminal.ViewportOffset;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.utils.Logger;

/**
 * @author Pontus Rydin
 */
public class FullscreenEditor extends EditorContext
{
    private static final class ViewportStackItem
    {
        private final ViewportStackItem m_previous;
        private final TerminalController m_controller;
        private final int m_maxY;
        
        public ViewportStackItem(final ViewportStackItem m_previous,
                final TerminalController m_controller, final int m_maxy)
        {
            super();
            this.m_previous = m_previous;
            this.m_controller = m_controller;
            m_maxY = m_maxy;
        }
        
        
        public TerminalController getController()
        {
            return m_controller;
        }
        public int getMaxY()
        {
            return m_maxY;
        }
        public ViewportStackItem getPrevious()
        {
            return m_previous;
        }
    }
    
    protected int m_cx = 0;
    protected int m_cy = 0;
    protected final int m_maxX;
    protected int m_maxY;
    // private final int m_yOffset = 0;
    protected TerminalController m_tc;
    protected int m_viewportStart = 0;
    private ViewportStackItem m_viewportStack;

    /**
     * 
     */
    public FullscreenEditor(Context context)
    throws IOException, UnexpectedException
    {
        super(context);
        m_tc = context.getTerminalController();
        TerminalSettings ts = context.getTerminalSettings();
        m_maxX = ts.getWidth() - 1;
        m_maxY = ts.getHeight() - 1;
        m_viewportStack = new ViewportStackItem(null, m_tc, m_maxY);
    }
    
	public UnstoredMessage edit()
	throws KOMException, InterruptedException
	{
	    TerminalController tc = this.getTerminalController();
	    MessageFormatter formatter = this.getMessageFormatter();
	    //tc.eraseScreen();
	    //tc.setCursor(0, 0);
	    this.refresh();
	    LineEditor in = this.getIn();
	    boolean pageBreak = in.getPageBreak();
	    in.setPageBreak(false);
	    try
	    {
	        this.mainloop();
	    }
		catch(IOException e) 
		{
			throw new KOMRuntimeException(formatter.format("error.reading.user.input"), e);		
		}
		finally
		{
		    in.setPageBreak(pageBreak);
		}
		

		return new UnstoredMessage(this.getSubject(), this.getBuffer().toString());
	}    
	
	public void pushViewport(int top, int bottom)
	{
	    m_viewportStack = new ViewportStackItem(m_viewportStack, m_tc, m_maxY);
	    m_tc = new ViewportOffset(m_tc, top, 0);
		m_maxY = bottom - top;
	}
	
	public void popViewport()
	{
	    m_tc = m_viewportStack.getController();
	    m_maxY = m_viewportStack.getMaxY();
	    m_viewportStack = m_viewportStack.getPrevious();
	}
	
	protected KeystrokeTokenizer getKeystrokeTokenizer()
	{
	    return this.getTerminalController().getKeystrokeTokenizer();
	}
	
	protected void unknownToken(KeystrokeTokenizer.Token token)
	throws EscapeException
	{
	    Logger.warn(this, "Unknown token " + token.getKind());
	}
    
    public void mainloop()
    throws InterruptedException, OperationInterruptedException, IOException
    {
        Buffer buffer = this.getBuffer();
        TerminalController tc = this.getTerminalController();
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
			tc.setCursor(0, 0);
			
			// Mainloop
			//
			String defaultLine = ""; 
			for(;;)
			{
				String line = null;
				try
				{
				    // Open up a new line if we've past end of buffer
				    //
				    if(m_cy >= buffer.size())
				        buffer.add("");
				    
				    // Retrieve line
				    //
				    // System.out.println("y: " + m_cy + ", viewport: " + m_viewportStart);
				    defaultLine = buffer.get(m_cy + m_viewportStart).toString();
				    int l = defaultLine.length();
				    if(l == 0)
				    {
				        m_tc.setCursor(m_cy, 0);
				        m_tc.eraseLine();
				    }
				    else
				    {
				        m_tc.setCursor(m_cy, l - 1);
				        m_tc.eraseToEndOfLine();
				        m_tc.setCursor(m_cy, 0);
				    }
				    out.flush();
				    int flags = LineEditor.FLAG_ECHO | LineEditor.FLAG_STOP_ON_EOL 
				    	| LineEditor.FLAG_DONT_REFRESH;
				    if(m_cy + m_viewportStart > 0)
				        flags |= LineEditor.FLAG_STOP_ON_BOL;
				    try
				    {
				        line = in.editLine(defaultLine, "", m_maxX, m_cx, flags);
				    }
				    catch(LineEditingDoneException e)
				    {
				        KeystrokeTokenizer.Token token = e.getToken();
				        line = e.getLine();
				        m_cx = e.getPos();
				        switch(token.getKind() & ~Keystrokes.TOKEN_MOFIDIER_BREAK)
				        {
				        case Keystrokes.TOKEN_UP:
				            buffer.set(m_cy + m_viewportStart, line);
				            this.moveUp();
				        	break;
				        case Keystrokes.TOKEN_DOWN:
				            buffer.set(m_cy + m_viewportStart, line);
				            this.moveDown();
				        	break;
				        case Keystrokes.TOKEN_CR:
				        { 
							// Add line to buffer
							//
				            String left = line.substring(0, m_cx);
				            String right = line.substring(m_cx);
				        	buffer.set(m_cy + m_viewportStart, left);
				        	boolean hadNewline = buffer.isNewline(m_cy + m_viewportStart); 
				        	buffer.setNewline(m_cy + m_viewportStart, true);

				        	// Insert the line
				        	//
				        	this.insertLine(right);
				        	buffer.setNewline(m_cy + m_viewportStart + 1, hadNewline);
				        	
				        	// ...and move cursor down.
				        	//
				        	this.moveDown();
							break;
				        }
				        case Keystrokes.TOKEN_DELETE_LINE:
				            this.deleteLine(m_cy + m_viewportStart, false);
				        	break;
				        case Keystrokes.TOKEN_DONE:
							buffer.set(m_cy + m_viewportStart, line);
							return;
						case Keystrokes.TOKEN_ABORT:
						    throw new OperationInterruptedException();
						case Keystrokes.TOKEN_REFRESH:
						    buffer.set(m_cy + m_viewportStart, line);
						    this.refresh();
						    break;
						default:
						    buffer.set(m_cy + m_viewportStart, line);
							try
							{
							    this.unknownToken(token);
							}
						    catch(SaveEditorException e1)
						    {
						        return;
						    }
						    catch(EscapeException e1)
						    {
						        // All other escapes abort
						        //
						        throw new OperationInterruptedException();
						    }

				        }
				    }
					catch(LineOverflowException e)
					{
						// Overflow! We have to wrap the line
						//
						String original = e.getLine();
						m_cx = e.getPos();
						buffer.set(m_cy + m_viewportStart, original);
						int bottom = this.wordWrap(m_cy + m_viewportStart);
						this.refreshRegion(m_cy, bottom);
						
						// Is the cursor in the wrapped part of the line? Move
						// down in that case
						//
						int ll = buffer.get(m_cy + m_viewportStart).length();
						if(m_cx > ll)
						{
						    m_cx -= ll;
						    this.moveDown();
						}
					}
					catch(LineUnderflowException e)
					{
					    // Delete past start of buffer? Not much to do!
					    //
					    int bufPos = m_cy + m_viewportStart;
					    if(bufPos <= 0)
					        continue;
					    // boolean newline = buffer.isNewline(bufPos);
					    String left = buffer.get(bufPos - 1);
					    buffer.set(bufPos - 1, left + e.getLine());
					    this.deleteLine(bufPos, true);
					    m_cx = left.length();
					}
				}
				catch(EventDeliveredException e)
				{
					// TODO: Handle chat messages here!
				}
				catch(StopCharException e)
				{
				    // Should not happen.
				    //
				    throw new RuntimeException("Should not happen", e);
				} 
			}
        }
        finally
        {
			// Erase screen
			//
			tc.eraseScreen();
			tc.setCursor(0, 0);
			
			// Pop input tokenizer
			//
            in.popTokenizer();
        }
    }
    
    protected void deleteLine(int line, boolean moveUp)
    {
        Buffer buffer = this.getBuffer();
	    buffer.remove(line);
	    
	    // Push line ending up
	    //
	    // buffer.setNewline(bufPos - 1)
	    
	    if(line > 0)
	        this.wordWrap(line - 1);
	    
	    // Move cursor up
	    //
	    boolean needsRefresh = m_cx == 0;
	    if(line > 0 && moveUp)
	        this.moveUp();
	    m_cx = 0;
	    
	    // Refresh. There are probably more efficient ways of doing this!
	    //
	    int lastLine = buffer.size() - m_viewportStart;
        m_tc.setCursor(lastLine, 0);
        m_tc.eraseLine();
        this.refreshViewport();        
    }
    
    protected int wordWrap(int line)
    {
        Buffer buffer = this.getBuffer();
        String left = "";
        boolean refreshAll = false;
        for(;;)
        {
            // Merge lines
            //
            if(line >= buffer.size() - 1)
        	    buffer.add("");
            String merged = left + buffer.get(line);
            int l = merged.length();
            
            // Word wrap if needed
            //
			WordWrapper wrapper = this.getWordWrapper(merged, m_maxX - 1);
			left = wrapper.nextLine();
			if(left == null)
			    break;
			buffer.set(line++, left);
			left = wrapper.nextLine();
			if(left == null)
			    break;
			
			// Did we wrap a line ending with a newline? Move the newline down
			// one line!
			//
			if(buffer.isNewline(line - 1))
			{
			    buffer.insertBefore(line, left);
			    left = "";
			    buffer.setNewline(line - 1, false);
			    buffer.setNewline(line, true);
			    ++line;
			    refreshAll = true;
			}
        }
        return refreshAll ? buffer.size() : line;
    }
    
    protected void insertLine(String line)
    {
    	// Scroll to open for new line. Scroll up if we're at 
    	// end of screen, otherwise scroll up.
    	//
        Buffer buffer = this.getBuffer();
    	if(m_cy <= m_maxY - 1)
    	    this.scrollRegionDown(m_cy + 1, m_maxY, 1);
    	else
    	    this.scrollRegionUp(0, m_maxY - 1, 1);
    	m_tc.eraseToEndOfLine();

    	m_cx = 0;
    	if(m_cy + m_viewportStart >= buffer.size() - 1)
    	    buffer.add(line);
    	else
    	    buffer.insertBefore(m_cy + m_viewportStart + 1, line);
    }
    
    protected void moveUp()
    {
        // Can we move up without scrolling?
        //
        if(m_cy > 0)
        {
            this.cursorUp(1);
            --m_cy;
        }
        else if(m_viewportStart > 0)
        {
            // We need to scroll, unless we're at the top of the buffer
            //
            this.scrollRegionDown(0, m_maxY, 1);
            m_tc.setCursor(m_cy, m_cx);
            --m_viewportStart;
        }
    }
    
    protected void moveDown()
    {
        if(this.isAtEndOfBuffer())
            return;
        if(!this.isAtEndOfScreen())
        {
            this.cursorDown(1);
            ++m_cy;
        }
        else
        {
            this.scrollRegionUp(0, m_maxY, 1);
            m_tc.setCursor(m_cy, m_cx);
            ++m_viewportStart;
        }
    }
    
    protected void cursorUp(int n)
    {
        if(m_tc.canUp())
            m_tc.up(n);
        else
            m_tc.setCursor(m_cy - n, 0);
    }
    
    protected void cursorDown(int n)
    {
        if(m_tc.canDown())
            m_tc.down(n);
        else
            m_tc.setCursor(m_cy + n, 0);
    }
        
    protected void scrollRegionDown(int start, int end, int n)
    {
        Buffer buffer = this.getBuffer();
        if(m_tc.canSetScrollRegion())
        {
            // We can do scroll regions!
            //
            m_tc.setScrollRegion(start, end);
            m_tc.setCursor(start + 1, m_cx);
            m_tc.scrollDown(n);
            m_tc.cancelScrollRegion();
        }
        else
        {
            // Can't do scroll regions. We need to redraw
            //
            int top = buffer.size();
            for(int idx = start; idx < end - n; ++idx)
            {
                m_tc.setCursor(idx + n, 0);
                m_tc.eraseLine();
                int p = idx + m_viewportStart;
                if(p >= top)
                    break;
                this.getOut().print(buffer.get(p).toString());
            }
        }
        m_tc.setCursor(m_cy, m_cx);
    }
    
    protected void scrollRegionUp(int start, int end, int n)
    {
        if(m_tc.canSetScrollRegion())
        {
            // We can do scroll regions!
            //
            m_tc.setScrollRegion(start, end);
            m_tc.setCursor(start + 1, m_cx);
            m_tc.scrollUp(n);
            m_tc.cancelScrollRegion();
        }
        else
        {
            // Can't do scroll regions. We need to redraw
            //
            for(int idx = start + n; idx < end; ++idx)
            {
                m_tc.setCursor(idx - n, 0);
                m_tc.eraseLine();
                this.getOut().print(this.getBuffer().get(idx + m_viewportStart).toString());                
            }
        }
        m_tc.setCursor(m_cy, m_cx);
    }
    
    public void revealCursor(boolean refresh)
    {
        // If cursor is outside visible area, move viewport and refresh
        //
        if(m_cy < m_maxY - 1)
            return;
        
        // Adjust viewport to fit on screen
        //
        int d = m_cy - (m_maxY - 1);
        m_cy -= d;
        m_viewportStart += d;        
        if(refresh)
            this.refreshViewport();
    }
    
    protected boolean isAtEndOfScreen()
    {
        return m_cy >= m_maxY - 1;
    }
    
    protected boolean isAtEndOfBuffer()
    {
        return m_viewportStart + m_cy >= this.getBuffer().size() - 1;
    }
    
	protected void refreshViewport()
	{
	    this.refreshRegion(0, m_maxY);
	}
        	
	protected void refresh()
	{
	    this.refreshRegion(0, m_maxY);
	}
	
	protected void refreshCurrentLine()
	{
        m_tc.setCursor(m_cy, 0);
        m_tc.eraseLine();
        this.getOut().print(this.getBuffer().get(m_viewportStart + m_cy));
	}
	
	protected void refreshRegion(int start, int end)
	{
	    Buffer buffer = this.getBuffer();
	    PrintWriter out = this.getOut();
	    // m_tc.eraseScreen();
	    int top =  end; //Math.min(end, buffer.size() - m_viewportStart); 
	    for(int idx = start; idx < top; ++idx)
	    {
	        m_tc.setCursor(idx, 0);
	        m_tc.eraseLine();
	        if(idx < buffer.size() - m_viewportStart)
	            out.print(buffer.get(m_viewportStart + idx));
	    }
	    m_tc.setCursor(m_cy, m_cx);
	    out.flush();
	}
}
