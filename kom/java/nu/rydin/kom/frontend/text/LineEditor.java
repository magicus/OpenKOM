/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import nu.rydin.kom.backend.EventSource;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.SessionShutdownEvent;
import nu.rydin.kom.exceptions.AmbiguousPatternException;
import nu.rydin.kom.exceptions.EventDeliveredException;
import nu.rydin.kom.exceptions.ImmediateShutdownException;
import nu.rydin.kom.exceptions.LineEditingDoneException;
import nu.rydin.kom.exceptions.LineEditingInterruptedException;
import nu.rydin.kom.exceptions.LineOverflowException;
import nu.rydin.kom.exceptions.LineUnderflowException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.OutputInterruptedException;
import nu.rydin.kom.exceptions.StopCharException;
import nu.rydin.kom.frontend.text.constants.Keystrokes;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.Logger;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class LineEditor implements NewlineListener
{
    public static final int TABSIZE						= 8;
    
	public static final int FLAG_STOP_ON_EVENT			= 0x01;
	public static final int FLAG_ECHO					= 0x02;
	public static final int FLAG_STOP_ON_BOL			= 0x04;
	public static final int FLAG_STOP_ON_EOL			= 0x08;
	public static final int FLAG_STOP_ONLY_WHEN_EMPTY	= 0x10;
	public static final int FLAG_RECORD_HISTORY			= 0x20;
	public static final int FLAG_ALLOW_HISTORY			= 0x40;
	public static final int FLAG_DONT_REFRESH			= 0x80;
    public static final int FLAG_TREAT_SPACE_AS_NEWLINE = 0x100;
	
	private static final char BELL 				= 7;
	private static final char BS				= 8;
	private static final char TAB				= 9;
	private static final char SPACE				= 32;
		
	/**
	 * Maximum number of keystrokes on one line before session
	 * shutdown. For protection against DOS-attacks.
	 */
	private static final int SHUTDOWN_LIMIT		= 10000;
	
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
			                "\u0003", 			// Ctrl-C
			                "\u0010",			// Ctrl-P
			                "\u000e"},			// Ctrl-N
			        new int[] {
			                Keystrokes.TOKEN_CR,			// Newline
			                Keystrokes.TOKEN_CR,			// CR
			                Keystrokes.TOKEN_BS,			// BS
			                Keystrokes.TOKEN_BS,			// DEL
			                Keystrokes.TOKEN_BOL,			// Ctrl-A
			                Keystrokes.TOKEN_EOL,			// Ctrl-E
			                Keystrokes.TOKEN_CLEAR_LINE, 	// Ctrl-U
			                Keystrokes.TOKEN_CLEAR_LINE, 	// Ctrl-X
			                Keystrokes.TOKEN_DELETE_WORD, 	// Ctrl-W
			                Keystrokes.TOKEN_UP, 			// <esc> [ A
			                Keystrokes.TOKEN_DOWN, 			// <esc> [ B
			                Keystrokes.TOKEN_RIGHT,			// <esc> [ C
			                Keystrokes.TOKEN_RIGHT, 		// Ctrl-F
			                Keystrokes.TOKEN_LEFT, 			// <esc> [ D
			                Keystrokes.TOKEN_LEFT,			// Ctrl-B
			                Keystrokes.TOKEN_ABORT,			// Ctrl-C
			                Keystrokes.TOKEN_PREV,			// Ctrl-P
			                Keystrokes.TOKEN_NEXT });		// Ctrl-N
	    }
	    catch(AmbiguousPatternException e)
	    {
	        throw new ExceptionInInitializerError(e);
	    }
	}
	
	private final Stack<KeystrokeTokenizer> m_tokenizerStack = new Stack<KeystrokeTokenizer>();
	
	private final ReaderProxy m_in;
	
	private final InputStream m_inStream;
	
	private final KOMWriter m_out;
	
	private final EventTarget m_target;
	
	private ServerSession m_session;
	
	private boolean m_dontCount = false;
	
	private int m_lineCount = 0;
	
	private boolean m_bypass = false;
	
	private final MessageFormatter m_formatter;
	
	private final String m_morePrompt;
	
	private long m_lastKeystrokeTime = System.currentTimeMillis();
	
	/**
	 * Proxy to a reader. This allows us to replace the underlying reader
	 * in order change the character set mapping.
	 * 
	 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
	 */
	private static class ReaderProxy
	{
		private Reader m_reader;
				
		public ReaderProxy(Reader reader)
		{
			m_reader = reader;
		}
		
		public void setReader(Reader reader)
		{
			m_reader = reader;
		}		
		
		public void close() throws IOException
		{
			m_reader.close();
		}

		public boolean equals(Object obj)
		{
			return m_reader.equals(obj);
		}

		public int hashCode()
		{
			return m_reader.hashCode();
		}

		public void mark(int readAheadLimit) throws IOException
		{
			m_reader.mark(readAheadLimit);
		}

		public boolean markSupported()
		{
			return m_reader.markSupported();
		}

		public int read() throws IOException
		{
			return m_reader.read();
		}

		public int read(char[] cbuf) throws IOException
		{
			return m_reader.read(cbuf);
		}

		public int read(char[] cbuf, int off, int len) throws IOException
		{
			return m_reader.read(cbuf, off, len);
		}

		public boolean ready() throws IOException
		{
			return m_reader.ready();
		}

		public void reset() throws IOException
		{
			m_reader.reset();
		}

		public long skip(long n) throws IOException
		{
			return m_reader.skip(n);
		}

		public String toString()
		{
			return m_reader.toString();
		}
	}
	
	private static class IOExceptionEvent extends Event
	{
		private IOException m_exception;
		
		public IOExceptionEvent(IOException exception)
		{
			m_exception = exception;
		}
		
		public IOException getException()
		{
			return m_exception;
		}
	}
	
	private abstract class LineEditorHelper extends Thread
	{
	    String m_threadName;
	    
	    public LineEditorHelper(String threadName) 
	    {
	        m_threadName = threadName;
	        setThreadName("not logged in");
	    }
	    
	    public void setThreadName(String userName) 
	    {
	        setName(m_threadName + " (" + userName + ")");
	    }
	    
		public void setSession(ServerSession session)
		{
		    setThreadName(session.getLoggedInUser().getUserid());
		}
	}
	
	private class EventPoller extends LineEditorHelper
	{
		private final int POLL_INTERVAL = ClientSettings.getEventPollInterval();
		
		public EventPoller() 
		{
			super("EventPoller");
		}
		
		public void run()
		{
			try
			{
			    EventSource es = LineEditor.this.m_session.getEventSource();
				for(;;)
				{
					Event e = es.pollEvent(POLL_INTERVAL);
					if(e != null)
						LineEditor.this.handleEvent(e);
				}
			}
			catch(InterruptedException e)
			{
				// Exiting gracefully...
				//
				Logger.debug(this, "Exiting event poller");
			}
		}
	}
	
	private class KeystrokePoller extends LineEditorHelper
	{
		public KeystrokePoller() 
		{
			super("KeystrokePoller");
		}
		
		public void run()
		{
			for(;;)
			{
				try
				{
					int ch = m_in.read();
					if(ch == -1)
					{
						// EOF on input. Kill session (if still alive)
						//
						LineEditor.this.handleEvent(new SessionShutdownEvent());
						break;
					}
					LineEditor.this.handleEvent(new KeystrokeEvent((char) ch)); 
				}
				catch(IOException e)
				{
					LineEditor.this.handleEvent(new IOExceptionEvent(e));
					break;
				}
			}	
			Logger.debug(this, "Exiting keystroke poller");		
		}		
	}
	
	/**
	 * Thread polling for system events, such as new messages and
	 * chat messages.
	 */ 
	private EventPoller m_eventPoller;
	
	/**
	 * Thread polling for keystrokes.
	 */
	private KeystrokePoller m_keystrokePoller;
	
	/**
	 * Incoming events. Low priority events that can't be handled 
	 * immediately are moved to the low priority queue.
	 */
	private final LinkedList<Event> m_eventQueue = new LinkedList<Event>();
	
	private final TerminalSettingsProvider m_tsProvider;
	
	private KeystrokeListener m_keystrokeListener;
	
	/**
	 * Command history
	 */
	private ArrayList<String> m_history = new ArrayList<String>();
		
	public LineEditor(InputStream in, KOMWriter out, EventTarget target, TerminalSettingsProvider tsProvider, 
	        ServerSession session, MessageFormatter formatter, String charset)
	throws UnsupportedEncodingException
	{
		m_in		= new ReaderProxy(new InputStreamReader(in, charset));
		m_inStream  = in;
		m_out		= out;
		m_target	= target;
		m_tsProvider= tsProvider;
		m_formatter	= formatter;
		
		// Create "more" prompt
		//
        m_morePrompt = m_formatter.format("misc.more");
		
		// Create tokenizer
		//
		m_tokenizerStack.push(s_tokenizerDef.createKeystrokeTokenizer());
		
		// Start pollers
		//
		if(session != null)
			this.setSession(session);
		
	}
	
	/**
	 * Starts the keystroke poller. No input can be read until this method is called.
	 */
	public void start()
	{
	    m_keystrokePoller = new KeystrokePoller();
	    m_keystrokePoller.start();
	}
	
	public void pushTokenizer(KeystrokeTokenizer tokenizer)
	{
	    m_tokenizerStack.push(tokenizer);
	}
	
	public void popTokenizer()
	{
	    m_tokenizerStack.pop();
	}
	
	public void setSession(ServerSession session)
	{
		if(m_session != null)
			throw new IllegalStateException("Already have a session!");
		m_session = session;
		m_keystrokePoller.setSession(session);
		m_eventPoller = new EventPoller();
		m_eventPoller.setSession(session);
		
		// Set priority somewhere between the normal and the maximum
		//
		m_eventPoller.setPriority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2);
		m_eventPoller.start();
		
	}
	
	public void setPageBreak(boolean flag)
	{
	    m_bypass = !flag;
	}
	
	public boolean getPageBreak()
	{
	    return !m_bypass;
	}
	
	public void setKeystrokeListener(KeystrokeListener listener)
	{
	    m_keystrokeListener = listener;
	}
	
	public String readLineStopOnEvent()
	throws IOException, InterruptedException, OperationInterruptedException, EventDeliveredException,
	LineEditingDoneException
	{
		try
		{
			return innerReadLine(null, null, 0, FLAG_STOP_ON_EVENT | FLAG_ECHO 
			        | FLAG_STOP_ONLY_WHEN_EMPTY);
		}
		catch(LineOverflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}
		catch(LineUnderflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}		
		catch(StopCharException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}				
	}
	
	public String readLineStopOnEvent(String defaultString)
	throws IOException, InterruptedException, OperationInterruptedException, EventDeliveredException,
	LineEditingDoneException
	{
		try
		{
			return innerReadLine(defaultString, null, 0, FLAG_STOP_ON_EVENT | FLAG_ECHO
			        | FLAG_STOP_ONLY_WHEN_EMPTY);
		}
		catch(LineOverflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}
		catch(LineUnderflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}
		catch(StopCharException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}				
	}	
	
	public void setCharset(String charset)
	throws UnsupportedEncodingException
	{
		m_in.setReader(new InputStreamReader(m_inStream, charset));
	}
	
	public String readPassword()
	throws IOException, InterruptedException, OperationInterruptedException, LineEditingDoneException
	{
		try
		{
			return innerReadLine(null, null, 0, 0);
		}
		catch(EventDeliveredException e)
		{
			// HUH??!?!? We asked NOT TO have events interrupt us, but
			// still here we are. Something is broken!
			//
			throw new RuntimeException("This should not happen!", e);
		}
		catch(LineOverflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}
		catch(LineUnderflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}
		catch(StopCharException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}						
	}
	
	public String readLine()
	throws IOException, InterruptedException, OperationInterruptedException,
	LineEditingDoneException
	{
		return this.readLine(null);
	}
	
	public String readLine(String defaultString)
	throws IOException, InterruptedException, OperationInterruptedException,
	LineEditingDoneException
	{
		try
		{
			return innerReadLine(defaultString, null, 0, FLAG_ECHO);
		}
		catch(EventDeliveredException e)
		{
			// HUH??!?!? We asked NOT TO have events interrupt us, but
			// still here we are. Something is broken!
			//
			throw new RuntimeException("This should not happen!", e);
		}
		catch(LineOverflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}
		catch(LineUnderflowException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}
		catch(StopCharException e)
		{
			throw new RuntimeException("This should not happen!", e);
		}				
	}
	
	public String readLine(String defaultString, String stopChars, int length, int flags)
	throws LineOverflowException, StopCharException, LineUnderflowException, IOException, 
	InterruptedException, OperationInterruptedException, EventDeliveredException,
	LineEditingDoneException
	{
		return innerReadLine(defaultString, stopChars, length, flags);
	}

	
	public int getChoice(String prompt, String[] choices, int defaultChoice, String errorString)
	throws IOException, InterruptedException, OperationInterruptedException,
	LineEditingDoneException
	{		String defaultString = defaultChoice != -1 ? choices[defaultChoice] : null;
		int top = choices.length;
		for(;;)
		{
			m_out.print(prompt);
			m_out.flush();
			String tmp = this.readLine(defaultString).toUpperCase();
			if(tmp.length() > 0)
			{
				for(int idx = 0; idx < top; ++idx)
				{
					if(choices[idx].toUpperCase().startsWith(tmp))
						return idx;
				}
			}
			
			// Invalid response
			//
			m_out.println();
			m_out.println(errorString);
			m_out.println();
		}		

	}
	
	public boolean getYesNo(String prompt, char[] yesChars, char[] noChars)
	throws IOException, InterruptedException
	{
		boolean result = false;
		
		//Print prompt
		m_out.print(prompt);
		m_out.flush();
		
			//Concatenate yesChars and noChars
			char[] tmp = new char[yesChars.length + noChars.length];
			System.arraycopy(yesChars, 0, tmp, 0, yesChars.length);
			System.arraycopy(noChars, 0, tmp, yesChars.length, noChars.length);
			

			char ch = waitForCharacter(tmp);
			for (int i = 0; i < yesChars.length; i++) {
				if (yesChars[i] == ch)
				{
					//We got a yes character!
					result = true;
				}
			}
			//No need to check for a no character since waitForChar won't return
			//on any other characters but the specified ones.
			
			// Erase the prompt
			//
			m_out.print('\r');
			PrintUtils.printRepeated(m_out, ' ', prompt.length());
			m_out.print('\r');
			m_out.flush();	
		return result;
	}
	
	/**
     * Displays a prompt and waits for user input. The user may choose to abort (noChars), 
     * continue another page (yesChars) or continue all (goAheadChars).
     * 
     * @param prompt
     * @return true = continue, false = abort
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean getMore(String prompt)
    throws IOException, InterruptedException
    {       
        boolean result = false;
        
        // Print prompt
        m_out.print(prompt);
        m_out.flush();
        
        String yes = m_formatter.format("misc.more.yeschars");
        String no = m_formatter.format("misc.more.nochars");
        String goAhead = m_formatter.format("misc.more.goaheadchars");

        // This is a quick fix, since the only alternative is completely rewriting
        // getMore() and a few other methods. The current architecture was never designed
        // for changing core constants (such as misc.more.yeschars) runtime.
        //
        boolean addSpaceAsYesChar = false;
        try
        {
            if ((this.m_session.getUser(this.m_session.getLoggedInUserId()).getFlags1() & UserFlags.TREAT_SPACE_AS_NEWLINE) != 0)
                addSpaceAsYesChar = true;
        }
        catch (Exception e)
        {
            // Quietly throw the exception away, there's nothing we can do anyway.
        }
        
        if (addSpaceAsYesChar)
        {
            // Space might be a no character by default, if so we need to drop it from that
            // string as well as adding it to the yes characters.
            //
            if (!yes.contains(" "))
            {
                yes += " ";
            }
            
            int idx = no.indexOf(' ');
            if (-1 != idx)
            {
                StringBuffer tmpNo = new StringBuffer(no);
                no = tmpNo.deleteCharAt(idx).toString();
            }
        }
        
        String all = yes + no + goAhead;
        
        char answer = waitForCharacter(all.toCharArray());
        if (yes.indexOf(answer) != -1) 
        {
            result = true;
        } 
        else if (goAhead.indexOf(answer) != -1)
        {
            result = true;
            // Disable page break for now. It will be enabled before the next command is executed
            // (see nu.rydin.kom.frontend.text.parser.Parser.ExecutableCommand.execute(Context))
            setPageBreak(false);
        }
        
        // No need to check for a no character since waitForChar won't
        // return
        // on any other characters but the specified ones.
            
        // Erase the prompt
        //
        m_out.print('\r');
        PrintUtils.printRepeated(m_out, ' ', prompt.length());
        m_out.print('\r');
        m_out.flush();  
        return result;
    }
    	
	
	/**
	 * Waits for the user to input any of the given characters and then returns it,
	 * or throws InputInterruptedException on ctrl-c.
	 */
	public char waitForCharacter(char[] allowedCharacters)
	throws IOException, InterruptedException
	{	
		//Loop until user inputs an expected character or input is interrupted
		//
		while(true)
		{
			try 
			{
				char ch = innerReadCharacter(0);
				for (int i = 0; i < allowedCharacters.length; i++) {
					if (ch == allowedCharacters[i])
					{
						return ch;
					}
				}
			} 
			catch (EventDeliveredException e) 
			{
				// HUH??!?!? We asked NOT TO have events interrupt us, but
				// still here we are. Something is broken!
				//
				throw new RuntimeException("This should not happen!", e);
			}
		}
	}
	
	/**
	 * Returns the next character from the input buffer or waits
	 * for a keystroke.
	 * 
	 * @param flags The flags
	 */
	public char readCharacter(int flags)
	throws IOException, InterruptedException, EventDeliveredException
	{
	    return this.innerReadCharacter(flags);
	}
	
	/**
	 * Reads a single character from the user without echoing back. Throws InputInterruptedException on ctrl-c.
	 * @param flags If FLAG_STOP_ON_EVENT is true, the method will throw event exception on event.
	 * @return The character read from user.
	 */
	protected char innerReadCharacter(int flags)
	throws IOException, InterruptedException, EventDeliveredException
	{
		while(true)
		{
			// Read next event from queue
			//
			Event ev = this.getNextEvent();
			
			// Not a keystroke? Handle event
			//
			if(!(ev instanceof KeystrokeEvent))
			{
				// IOException while reading user input? Pass it on!
				//
				if(ev instanceof IOExceptionEvent)
				{
					throw ((IOExceptionEvent) ev).getException();
				}
				
				// Session shutdown? Get us out of here immediately!
				//
				if(ev instanceof SessionShutdownEvent)
					throw new InterruptedException();

				// Dispatch event
				//
				ev.dispatch(m_target);
				if((flags & FLAG_STOP_ON_EVENT) != 0)
					throw new EventDeliveredException(ev, "", -1);
				
				// This is not the event we're looking for. Move along.
				//
				continue;
			}
			
			// Getting here means we have a Keystroke event, let's handle it!
			//
			m_lineCount = 0;
			char ch = ((KeystrokeEvent) ev).getChar();
			m_lastKeystrokeTime = System.currentTimeMillis();
			if(m_keystrokeListener != null)
			    m_keystrokeListener.keystroke(ch);
			return ch;
		}
	}
	
	protected String innerReadLine(String defaultString, String stopChars, int maxLength, int flags)
	throws EventDeliveredException, StopCharException, LineOverflowException, LineUnderflowException, 
	IOException, OperationInterruptedException, InterruptedException, LineEditingDoneException
	{
	    return this.editLine(defaultString, stopChars, maxLength, -1, flags);
	}
	
	public String editLine(String defaultString, int maxLength, int pos, int flags)
	throws EventDeliveredException, StopCharException, LineOverflowException, LineUnderflowException, 
	IOException, OperationInterruptedException, InterruptedException, LineEditingDoneException
	{
	    return this.editLine(defaultString, "", maxLength, pos, flags);
	}
	
	public KeystrokeTokenizer.Token readToken(int flags)
	throws EventDeliveredException, InterruptedException, IOException
	{
	    KeystrokeTokenizer tokenizer = m_tokenizerStack.peek();
	    KeystrokeTokenizer.Token token = null;
	    while(token == null)
	    {
		    char ch;
		    for(;;)
	        {
		        try
		        {
		            ch = this.innerReadCharacter(flags);
		            break;
		        }
		        catch(EventDeliveredException e)
		        {
		            if((flags & FLAG_STOP_ONLY_WHEN_EMPTY) == 0)
		                throw e;
		        }
	        }
	        token = tokenizer.feedCharacter(ch);
	    }
	    return token;
	}
	
	public String editLine(String defaultString, String stopChars, int maxLength, int pos, int flags)
	throws EventDeliveredException, StopCharException, LineOverflowException, LineUnderflowException, 
	IOException, OperationInterruptedException, InterruptedException, LineEditingDoneException
	{
	    /**
	     * Position in history buffer
	     */
	    int historyPos = m_history.size();
		StringBuffer buffer = new StringBuffer(500);
		int cursorpos = 0;
		boolean newLine = false;
		
		// Did we get a default string?
		//
		if(defaultString != null)
		{
			// Strip newlines
			//
		   
			if(defaultString.endsWith("\n"))
			{
				defaultString = defaultString.substring(0, defaultString.length() - 1);
				newLine = true;
			}
			buffer.append(defaultString);
			m_out.print(defaultString);
			cursorpos = defaultString.length();
			if(pos != -1)
			{
			    for(;cursorpos > pos; --cursorpos)
			        m_out.print(BS);
			}
			m_out.flush();
			
		}
		
		int count = 0;
		boolean editing = true;
		while(editing)
		{
			// Getting here means that we have a KeyStrokeEvent. Handle it!
			//
		    KeystrokeTokenizer.Token token = null;
		    char ch = 0;
		    while(token == null)
		    {
		        for(;;)
		        {
			        try
			        {
			            ch = this.innerReadCharacter(flags);
			            if(++count > SHUTDOWN_LIMIT)
			            {
			                m_out.println();
			                m_out.println("Suspected DOS-attack!!! Shutting down!");
			                throw new InterruptedException();
			            }
			            break;
			        }
			        catch(EventDeliveredException e)
			        {
			            if((flags & FLAG_STOP_ONLY_WHEN_EMPTY) == 0)
			                throw e;
			            else
			            {
			                if(buffer.length() == 0)
			                    throw e;
			                
			                // Otherwise, skip and hope it's queued
			                //
			            }
			        }
		        }
		        token = m_tokenizerStack.peek().feedCharacter(ch);
		    }
		    int kind = token.getKind();
		    boolean exit = (kind & Keystrokes.TOKEN_MOFIDIER_BREAK) != 0;
		    kind &= ~Keystrokes.TOKEN_MOFIDIER_BREAK; // Strip break bit
		    
		    // Break?
		    //
		    if(exit)
		    {
		        String line = buffer.toString();
		        throw new LineEditingDoneException(token, newLine ? line + '\n' : line , cursorpos);
		    }
		    switch(kind)
		    {
		    	case Keystrokes.TOKEN_ABORT:
	    	        throw new LineEditingInterruptedException(buffer.toString());
		    	case Keystrokes.TOKEN_PREV:
		    	case Keystrokes.TOKEN_UP:
		    	    if(historyPos > 0 && (flags & FLAG_ALLOW_HISTORY) != 0)
		    	    {
				    	this.deleteLine(buffer, cursorpos);
						cursorpos = 0;						
						String command = m_history.get(--historyPos);
		    	        buffer.append(command);
		    	        m_out.print(command);
		    	        cursorpos = command.length();
		    	    }
		    	    else
		    	        m_out.write(BELL);
		    		break;
		    	case Keystrokes.TOKEN_NEXT:
                case Keystrokes.TOKEN_DOWN:
                    if(historyPos < m_history.size() && (flags & FLAG_ALLOW_HISTORY) != 0)
                    {
                        this.deleteLine(buffer, cursorpos);
                        cursorpos = 0;
                        historyPos++;
                        if (historyPos < m_history.size())
                        {
                            String command = m_history.get(historyPos);
                            buffer.append(command);
                            m_out.print(command);
                            cursorpos = command.length();
                        }
                    }
                    else
                        m_out.write(BELL);
                    break;
	    		case Keystrokes.TOKEN_RIGHT:
	    		    if(cursorpos==buffer.length() || (flags & FLAG_ECHO) == 0)
		    		{
		    			//If at end of buffer or no echo, ignore arrow and beep.
		    			m_out.write(BELL);
		    		}
		    		else
		    		{
						m_out.write(buffer.charAt(cursorpos));
						cursorpos++;
		    		}
	    		    break;
	    		case Keystrokes.TOKEN_LEFT:
					if(cursorpos == 0 || (flags & FLAG_ECHO) == 0)
					{
					    //If at end of buffer or no echo, ignore arrow and beep.
					    //
						if((flags & FLAG_STOP_ON_BOL) != 0) 
						{
						    //FIXME Disabled this, breaks lots of stuff. :-)
							//throw new LineUnderflowException();
						}
						m_out.write(BELL);
					}
					else
					{
						cursorpos--;
						m_out.write(BS);
					}
					break;
				case Keystrokes.TOKEN_CR:
					editing = false;
					m_out.write('\r');					
					m_out.write('\n');
					break;
				case Keystrokes.TOKEN_BS:
					if(cursorpos == 0)
					{
						if((flags & FLAG_STOP_ON_BOL) != 0)
							throw new LineUnderflowException(buffer.toString(), cursorpos);
						m_out.write(BELL);
					}
					else
					{
						buffer.deleteCharAt(--cursorpos);
						m_out.write(BS);
						m_out.write(SPACE);
						m_out.write(BS);
						if (cursorpos < buffer.length())
						{
							m_out.write(buffer.substring(cursorpos));
							m_out.write(' ');
							PrintUtils.printRepeated(m_out, BS, buffer.length() - cursorpos + 1);
						}
					}	
					break;
				case Keystrokes.TOKEN_BOL:
					if(((flags & FLAG_ECHO) != 0) && cursorpos > 0)
					{
						//Advance cursor to beginning of line
						PrintUtils.printRepeated(m_out, BS, cursorpos);
						cursorpos = 0;
					}
					break;
				case Keystrokes.TOKEN_EOL:
					if(((flags & FLAG_ECHO) != 0) && cursorpos < buffer.length())
					{
					    // Advance cursor to end of line
					    //
						m_out.write(buffer.substring(cursorpos));
						cursorpos = buffer.length();
					}
					break;
				case Keystrokes.TOKEN_CLEAR_LINE:
					{
				    	this.deleteLine(buffer, cursorpos);
						cursorpos = 0;						
						break;
					}					
				case Keystrokes.TOKEN_DELETE_WORD:
				{
					//Move i to beginning of current word.
					int i = cursorpos;
					for(; i > 0 && Character.isSpaceChar(buffer.charAt(i - 1)); --i)
					{
					}
					for(; i > 0 && !Character.isSpaceChar(buffer.charAt(i - 1)); --i)
					{
					}
					
					if (i != cursorpos)
					{
						//Delete characters in buffer from i to cursorpos.
						buffer.delete(i, cursorpos);
						
						//Move back cursor to i.
						PrintUtils.printRepeated(m_out, BS, cursorpos - i);
						
						//Print rest of buffer from i.
						m_out.write(buffer.substring(i));
						
						//Pad with spaces.
						PrintUtils.printRepeated(m_out, SPACE, cursorpos - i);
						
						//Move back cursor to i.
						PrintUtils.printRepeated(m_out, BS, buffer.length() + (cursorpos - i) - i);
						
						//Set new cursorpos
						cursorpos = i;
					}
					break;				
				}
				case Keystrokes.TOKEN_SKIP:
				    break;
				case KeystrokeTokenizerDefinition.LITERAL:
					if(stopChars != null && stopChars.indexOf(ch) != -1)
					    throw new StopCharException(buffer.toString(), cursorpos, ch);

					// Are we exceeding max length?
					//
					if(maxLength != 0 && buffer.length() >= maxLength - 1)
					{
						// Break on overflow?
						//
						if((flags & FLAG_STOP_ON_EOL) != 0)
						{
							buffer.insert(cursorpos++, ch);
							throw new LineOverflowException(buffer.toString(), cursorpos);
						}
						// Don't break. Just make noise and ignore keystroke
						//
						m_out.write(BELL);
						m_out.flush();
						break; 
					}
                    
                    if (((flags & FLAG_TREAT_SPACE_AS_NEWLINE) != 0) && (0 == cursorpos) && SPACE == ch)
                    {
                        m_out.write("\r\n");
                        return "\n"; // execute default command
                    }
                    
					// Skip non printable characters
					//
					if(ch < 32 && ch != TAB)
					    continue;
					
					// TAB needs special treatment. Insert enough spaces to get to
					// the next position that's a multiple of TABSIZE
					//
					if(ch == TAB)
					{
						int spaces = TABSIZE - (cursorpos % TABSIZE);
						for(int idx = 0; idx < spaces; ++idx)
						{
							buffer.insert(cursorpos++, ' ');
							m_out.write(' ');
						}
					}
					else
					    buffer.insert(cursorpos++, ch);
						
					if((flags & FLAG_ECHO) != 0) 
					{
					    if(ch != TAB)
					        m_out.write(ch);
						if (cursorpos < buffer.length())
						{
							m_out.write(buffer.substring(cursorpos));
							m_out.write(SPACE);
							PrintUtils.printRepeated(m_out, BS, buffer.length() - cursorpos + 1);
						}
					}
					else 
						m_out.write('*');
					break;
		    }
			m_out.flush();
		}		
		// We got a string! Should we record it in command history?
		//
		String answer = buffer.toString();
		
		// Don't store if not asked to or if previous command
		// was exactly the same.
		//
		if((flags & FLAG_RECORD_HISTORY) != 0 && buffer.length() > 0 && 
		        (m_history.size() == 0 || !answer.equals(m_history.get(m_history.size() - 1))))
		    m_history.add(answer);
		return newLine ? answer + '\n' : answer;
	}
	
	private void deleteLine(StringBuffer buffer, int cursorpos)
	{
		int top = buffer.length();
		
		// Advance cursor to end of line
		//
		int n = top  - cursorpos;
		while(n-- > 0)
			m_out.write(SPACE);
		
		// Delete to beginning of line
		//
		n = top;
		while(n-- > 0)
		{
			m_out.write(BS);
			m_out.write(SPACE);
			m_out.write(BS);
		}
		buffer.delete(0, buffer.length());				
	}
	
	public void shutdown()
	{
	    if(m_eventPoller != null)
	        m_eventPoller.interrupt();
		if(m_keystrokePoller != null)
		    m_keystrokePoller.interrupt();
		m_eventPoller = null;
		m_keystrokePoller = null;
	}

	protected synchronized void handleEvent(Event e)
	{
		m_eventQueue.addLast(e);
		this.notify();
	}
	
	protected synchronized Event getNextEvent()
	throws InterruptedException
	{
		while(m_eventQueue.isEmpty())
			this.wait();
		return m_eventQueue.removeFirst();
	}	
	
	public long getLastKeystrokeTime()
	{
	    return m_lastKeystrokeTime;
	}
	
	public void resetLineCount()
	{
	    m_lineCount = 0;
	}
	
	// Implementation of NewlineListener
	//
	public void onNewline()
	{
	    try
	    {
		    if(m_dontCount || m_bypass)
		        return;
		    if(++m_lineCount >= m_tsProvider.getTerminalSettings().getHeight() - 1)
		    {
		        m_dontCount = true;
		        if(!this.getMore(m_morePrompt))
		            throw new OutputInterruptedException();
		        m_lineCount = 0;
		    }
	    }
	    catch(InterruptedException e)
	    {
	        // We can't throw an InterruptedException here, since it would have to be
	        // passed through the Writer interface.
	        //
	        // Don't page-break after this, since it could interfere with a session
	        // trying to wind down it's operations.
	        //
	        m_bypass = true;
	        throw new ImmediateShutdownException();
	    }
	    catch(IOException e)
	    {
	        throw new RuntimeException(e);
	    }
	    finally
	    {
	        m_dontCount = false;
	    }
	}
}
