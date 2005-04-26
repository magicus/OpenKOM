/*
 * Created on Jan 30, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.fullscreen;

import java.io.IOException;
import java.io.PrintWriter;

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
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.TerminalSettings;
import nu.rydin.kom.frontend.text.constants.Keystrokes;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Message;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.text.terminal.TerminalController;
import nu.rydin.kom.text.terminal.ViewportOffset;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Pontus Rydin
 */
public class FullscreenEditor implements MessageEditor
{
    private EditorContext m_context;
    
    private int m_cx = 0;
    private int m_cy = 0;
    private final int m_maxX;
    private int m_maxY;
    // private final int m_yOffset = 0;
    private TerminalController m_tc;
    private int m_viewportStart = 0;
    private Buffer m_buffer;
    
    private final static int NUM_HEADER_ROWS = 3;

    /**
     * 
     */
    public FullscreenEditor(Context context)
    throws IOException, UnexpectedException
    {
        m_context = new EditorContext(context);
        m_tc = context.getTerminalController();
        m_buffer = m_context.getBuffer();
        TerminalSettings ts = context.getTerminalSettings();
        m_maxX = ts.getWidth() - 1;
        m_maxY = ts.getHeight() - 1;
    }

    public UnstoredMessage edit(long replyTo) throws KOMException,
            InterruptedException, IOException
    {
        DisplayController dc = m_context.getDisplayController();
		PrintWriter out = m_context.getOut();
		LineEditor in = m_context.getIn();
		MessageFormatter formatter = m_context.getMessageFormatter();	
		String oldSubject = null;
		try
		{
		    m_tc.eraseScreen();
		    m_tc.setCursor(0, 0);
			dc.messageHeader();
			
			// Print author
			//
			// out.println(formatter.format("simple.editor.author", m_context.getCachedUserInfo().getName()));

			// FIXME EDITREFACTOR: This whole thing does very unneccessary lookups since the editor doesn't hold enough information about the message being replied to.
			// Handle reply
			//
			if(replyTo != -1)
			{
			    // Fetch reply-to
			    //
			    Message oldMessage = m_context.getSession().innerReadMessage(replyTo).getMessage();
			    MessageOccurrence oldMessageOcc = m_context.getSession().getMostRelevantOccurrence(m_context.getSession().getCurrentConferenceId(), replyTo); 
			    
			    // Fetch old subject
			    //
			    oldSubject = oldMessage.getSubject();
			    
				if(m_context.getRecipient().getId() == oldMessageOcc.getConference())
				{
					// Simple case: Original text is in same conference
					//
					out.println(formatter.format("CompactMessagePrinter.reply.to.same.conference", 
						new Object[] { new Long(oldMessageOcc.getLocalnum()), 
							m_context.formatObjectName(oldMessage.getAuthorName(), oldMessage.getAuthor()) } ));		
				}
				else
				{
					// Complex case: Original text was in a different conference
					//
					out.println(formatter.format("CompactMessagePrinter.reply.to.different.conference", 
						new Object[] { new Long(oldMessageOcc.getLocalnum()),
					        m_context.formatObjectName(m_context.getSession().getConference(oldMessageOcc.getConference()).getName(),
					        oldMessageOcc.getConference()), 
					        m_context.formatObjectName(oldMessage.getAuthorName(), oldMessage.getAuthor()) }));
				}
			}
			
			// Print receiver
			//
			out.println(formatter.format("simple.editor.receiver", m_context.formatObjectName(m_context.getRecipient())));
			
			// Read subject
			//
			String subjLine = formatter.format("simple.editor.subject");
			out.print(subjLine);
			dc.input();
			out.flush();
			m_context.setSubject(in.readLine(oldSubject));
			dc.messageHeader();
			PrintUtils.printRepeated(out, '-', subjLine.length() + m_context.getSubject().length());
			out.println();
			
			// Establish viewport
			//
			int headerRows = NUM_HEADER_ROWS;
			if(replyTo != -1)
			    ++headerRows;
			m_tc = new ViewportOffset(m_tc, headerRows, 0);
			m_maxY -= headerRows;
						
			// Enter the main editor loop
			//
			this.mainloop();
			return new UnstoredMessage(m_context.getSubject(), m_context.getBuffer().toString());
		}
		catch(IOException e) 
		{
			throw new KOMRuntimeException(formatter.format("error.reading.user.input"), e);		
		}
    }
    
    public void mainloop()
    throws InterruptedException, OperationInterruptedException, IOException
    {
        TerminalController tc = m_context.getTerminalController();
        DisplayController dc = m_context.getDisplayController();
        dc.messageBody();
        LineEditor in = m_context.getIn();
        in.pushTokenizer(tc.getKeystrokeTokenizer());
        try
        {
			// Set up some stuff
			//
			PrintWriter out = m_context.getOut();
			
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
				    if(m_cy >= m_buffer.size())
				        m_buffer.add("");
				    
				    // Retrieve line
				    //
				    // System.out.println("y: " + m_cy + ", viewport: " + m_viewportStart);
				    defaultLine = m_buffer.get(m_cy + m_viewportStart).toString();
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
				            m_buffer.set(m_cy + m_viewportStart, line);
				            this.moveUp();
				        	break;
				        case Keystrokes.TOKEN_DOWN:
				            m_buffer.set(m_cy + m_viewportStart, line);
				            this.moveDown();
				        	break;
				        case Keystrokes.TOKEN_CR:
				        { 
							// Add line to buffer
							//
				            String left = line.substring(0, m_cx);
				            String right = line.substring(m_cx);
				        	m_buffer.set(m_cy + m_viewportStart, left);
				        	m_buffer.setNewline(m_cy + m_viewportStart, true);

				        	// Insert the line
				        	//
				        	this.insertLine(right);
				        	
				        	// ...and move cursor down.
				        	//
				        	this.moveDown();
							break;
				        }
				        case Keystrokes.TOKEN_DELETE_LINE:
				            this.deleteLine(m_cy + m_viewportStart, false);
				        	break;
				        case Keystrokes.TOKEN_DONE:
							m_buffer.set(m_cy + m_viewportStart, line);
							return;
						case Keystrokes.TOKEN_ABORT:
						    throw new OperationInterruptedException();
						case Keystrokes.TOKEN_REFRESH:
						    m_buffer.set(m_cy + m_viewportStart, line);
						    this.refresh();
						    break;
				        }
				    }
					catch(LineOverflowException e)
					{
						// Overflow! We have to wrap the line
						//
						String original = e.getLine();
						m_cx = e.getPos();
						m_buffer.set(m_cy + m_viewportStart, original);
						int bottom = this.wordWrap(m_cy + m_viewportStart);
						this.refreshRegion(m_cy, bottom);
						
						// Is the cursor in the wrapped part of the line? Move
						// down in that case
						//
						int ll = m_buffer.get(m_cy + m_viewportStart).length();
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
					    // boolean newline = m_buffer.isNewline(bufPos);
					    String left = m_buffer.get(bufPos - 1);
					    m_buffer.set(bufPos - 1, left + e.getLine());
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
	    m_buffer.remove(line);
	    
	    // Push line ending up
	    //
	    // m_buffer.setNewline(bufPos - 1)
	    
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
	    int lastLine = m_buffer.size() - m_viewportStart;
        m_tc.setCursor(lastLine, 0);
        m_tc.eraseLine();
        this.refresh();        
    }
    
    protected int wordWrap(int line)
    {
        String left = "";
        boolean refreshAll = false;
        for(;;)
        {
            // Merge lines
            //
            if(line >= m_buffer.size() - 1)
        	    m_buffer.add("");
            String merged = left + m_buffer.get(line);
            int l = merged.length();
            
            // Word wrap if needed
            //
			WordWrapper wrapper = m_context.getWordWrapper(merged, m_maxX - 1);
			left = wrapper.nextLine();
			if(left == null)
			    break;
			m_buffer.set(line++, left);
			left = wrapper.nextLine();
			if(left == null)
			    break;
			
			// Did we wrap a line ending with a newline? Move the newline down
			// one line!
			//
			if(m_buffer.isNewline(line - 1))
			{
			    m_buffer.insertBefore(line, left);
			    left = "";
			    m_buffer.setNewline(line - 1, false);
			    m_buffer.setNewline(line, true);
			    ++line;
			    refreshAll = true;
			}
        }
        return refreshAll ? m_buffer.size() : line;
    }
    
    protected void insertLine(String line)
    {
    	// Scroll to open for new line. Scroll up if we're at 
    	// end of screen, otherwise scroll up.
    	//
    	if(m_cy <= m_maxY - 1)
    	    this.scrollRegionDown(m_cy + 1, m_maxY, 1);
    	else
    	    this.scrollRegionUp(0, m_maxY - 1, 1);
    	m_tc.eraseToEndOfLine();

    	m_cx = 0;
    	if(m_cy + m_viewportStart >= m_buffer.size() - 1)
    	    m_buffer.add(line);
    	else
    	    m_buffer.insertBefore(m_cy + m_viewportStart + 1, line);
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
            int top = m_buffer.size();
            for(int idx = start; idx < end - n; ++idx)
            {
                m_tc.setCursor(idx + n, 0);
                m_tc.eraseLine();
                int p = idx + m_viewportStart;
                if(p >= top)
                    break;
                m_context.getOut().print(m_buffer.get(p).toString());
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
                m_context.getOut().print(m_buffer.get(idx + m_viewportStart).toString());                
            }
        }
        m_tc.setCursor(m_cy, m_cx);
    }
    
    protected boolean isAtEndOfScreen()
    {
        return m_cy >= m_maxY - 1;
    }
    
    protected boolean isAtEndOfBuffer()
    {
        return m_viewportStart + m_cy >= m_buffer.size() - 1;
    }
        
	public void setRecipient(NameAssociation recipient)
	{
	    m_context.setRecipient(recipient);
	}
	
	public NameAssociation getRecipient()
	{
	    return m_context.getRecipient();
	}
	
	public void setReplyTo(long replyTo)
	{
	    m_context.setReplyTo(replyTo);
	}
	
	protected void refresh()
	{
	    this.refreshRegion(0, m_maxY);
	}
	
	protected void refreshRegion(int start, int end)
	{
	    PrintWriter out = m_context.getOut();
	    // m_tc.eraseScreen();
	    int top = Math.min(end, m_buffer.size() - m_viewportStart); 
	    for(int idx = start; idx < top; ++idx)
	    {
	        m_tc.setCursor(idx, 0);
	        m_tc.eraseLine();
	        out.print(m_buffer.get(m_viewportStart + idx));
	    }
	    m_tc.setCursor(m_cy, m_cx);
	    out.flush();
	}
}
