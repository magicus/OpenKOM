/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import nu.rydin.kom.EventDeliveredException;
import nu.rydin.kom.LineOverflowException;
import nu.rydin.kom.LineUnderflowException;
import nu.rydin.kom.OutputInterruptedException;
import nu.rydin.kom.StopCharException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.SessionShutdownEvent;
import nu.rydin.kom.frontend.text.ansi.ANSISequences;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class LineEditor implements NewlineListener
{
	public static final int FLAG_STOP_ON_EVENT	= 0x01;
	public static final int FLAG_ECHO			= 0x02;
	public static final int FLAG_STOP_ON_BOL	= 0x04;
	public static final int FLAG_STOP_ON_EOL	= 0x08;	
	
	private static final char BELL 				= 7;
	private static final char BS				= 8;
	private static final char CTRL_A			= 1;
	private static final char CTRL_C			= 3;
	private static final char CTRL_E			= 5;
	private static final char CTRL_U			= 21;
	private static final char CTRL_W			= 23;
	private static final char CTRL_X			= 24;
	private static final char DEL				= 127;
	private static final char ESC				= 27;
	private static final char NEWLINE			= 14;
	private static final char RET				= 18;
	private static final char SPACE				= 32;
	
	private final ReaderProxy m_in;
	
	private final InputStream m_inStream;
	
	private final KOMWriter m_out;
	
	private final EventTarget m_target;
	
	private ServerSession m_session;
	
	private boolean m_dontCount = false;
	
	private int m_lineCount = 0;
	
	private final MessageFormatter m_formatter;
	
	private final String m_morePrompt;
	
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
	
	private class EventPoller extends Thread
	{
		private final int POLL_INTERVAL = ClientSettings.getEventPollInterval();
		
		public void run()
		{
			try
			{
				for(;;)
				{
					Event e = LineEditor.this.m_session.pollEvent(POLL_INTERVAL);
					if(e != null)
						LineEditor.this.handleEvent(e);
				}
			}
			catch(InterruptedException e)
			{
				// Exiting gracefully...
				//
				System.out.println("Exiting event poller...");
			}
		}
	}
	
	private class KeystrokePoller extends Thread
	{
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
			System.out.println("Exiting keystroke poller...");		
		}		
	}
	
	/**
	 * Thread polling for system events, such as new messages and
	 * chat messages.
	 */
	private final EventPoller m_eventPoller = new EventPoller();
	
	/**
	 * Thread polling for keystrokes.
	 */
	private final KeystrokePoller m_keystrokePoller = new KeystrokePoller();
	
	/**
	 * Incoming events. Low priority events that can't be handled 
	 * immediately are moved to the low priority queue.
	 */
	private final LinkedList m_eventQueue = new LinkedList();
	
	private final TerminalSettingsProvider m_tsProvider;
	
		
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
		// Getting the more prompt
		//
		StringBuffer sb = new StringBuffer();
		sb.append(m_formatter.format("misc.more"));
		sb.append(" (");
		sb.append(m_formatter.format("misc.y"));
		sb.append("/");
		sb.append(m_formatter.format("misc.n"));
		sb.append(") ");
		m_morePrompt = sb.toString();
		
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
	    m_keystrokePoller.start();
	}
	
	public void setSession(ServerSession session)
	{
		if(m_session != null)
			throw new IllegalStateException("Already have a session!");
		m_session = session;
		m_eventPoller.start();
		
	}
	
	public String readLineStopOnEvent()
	throws IOException, InterruptedException, EventDeliveredException
	{
		try
		{
			return innerReadLine(null, null, 0, FLAG_STOP_ON_EVENT | FLAG_ECHO);
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
	throws IOException, InterruptedException, EventDeliveredException
	{
		try
		{
			return innerReadLine(defaultString, null, 0, FLAG_STOP_ON_EVENT | FLAG_ECHO);
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
	throws IOException, InterruptedException
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
	throws IOException, InterruptedException
	{
		return this.readLine(null);
	}
	
	public String readLine(String defaultString)
	throws IOException, InterruptedException
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
	throws LineOverflowException, StopCharException, LineUnderflowException, IOException, InterruptedException, EventDeliveredException
	{
		return innerReadLine(defaultString, stopChars, length, flags);
	}

	
	public int getChoice(String prompt, String[] choices, int defaultChoice, String errorString)
	throws IOException, InterruptedException
	{		String defaultString = defaultChoice != -1 ? choices[defaultChoice] : null;
		int top = choices.length;
		for(;;)
		{
			m_out.print(prompt);
			m_out.flush();
			String tmp = this.readLine(defaultString).toUpperCase();
			int choice = -1;
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
			PrintUtils.printRepeated(m_out, ' ', m_morePrompt.length());
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
				{
					throw new InterruptedException();
				}

				//Dispatch event
				//
				ev.dispatch(m_target);
				if((flags & FLAG_STOP_ON_EVENT) != 0)
					throw new EventDeliveredException(ev);
				
				// This is not the event we're looking for. Move along.
				//
				continue;
			}
			
			// Getting here means we have a Keystroke event, let's handle it!
			//
			m_lineCount = 0;
			return ((KeystrokeEvent) ev).getChar();
		}
	}
	
	protected String innerReadLine(String defaultString, String stopChars, int maxLength, int flags)
	throws EventDeliveredException, StopCharException, LineOverflowException, LineUnderflowException, 
	IOException, InterruptedException
	{
		StringBuffer buffer = new StringBuffer(500);
		int cursorpos = 0;
		
		// Did we get a default string?
		//
		if(defaultString != null)
		{
			// Strip newlines
			//
			if(defaultString.endsWith("\n"))
				defaultString = defaultString.substring(0, defaultString.length() - 1);
			buffer.append(defaultString);
			m_out.print(defaultString);
			m_out.flush();
			cursorpos = defaultString.length();
		}
				
		boolean escapemode = false;
		String escapecode = "";
		
		boolean editing = true;
		while(editing)
		{
			// Getting here means that we have a KeyStrokeEvent. Handle it!
			//
		    char ch = this.innerReadCharacter(flags);
		    
		    if (escapemode)
		    {
		    	if ((ch == '[') || (ch == ';') || Character.isDigit(ch))
		    	{
		    		//We got escape code data, store and continue.
		    		escapecode += ch;
		    	}
		    	else if (Character.isLetter(ch))
		    	{
		    		//We got a letter, store and mark end of escape sequence.
		    		escapecode += ch;
		    		escapemode = false;
		    		
		    		//Handle escape-code
		    		if ("[A".equals(escapecode))
		    		{
		    			//Arrow up
		    			m_out.write(BELL);
		    		}
		    		else if ("[B".equals(escapecode))
		    		{
		    			//Arrow down
		    			m_out.write(BELL);
		    		}
		    		else if ("[C".equals(escapecode))
		    		{
		    			//Arrow right
		    			if(cursorpos==buffer.length() || (flags & FLAG_ECHO) == 0)
		    			{
		    				//If at end of buffer or no echo, ignore arrow and beep.
		    				m_out.write(BELL);
		    			}
		    			else
		    			{
		    				cursorpos++;
							m_out.write(ANSISequences.MOVE_CURSOR_RIGHT_ONE);
		    			}
		    		}
		    		else if ("[D".equals(escapecode))
		    		{
		    			//Arrow left
						if(cursorpos == 0 || (flags & FLAG_ECHO) == 0)
						{
//							If at end of buffer or no echo, ignore arrow and beep.
							if((flags & FLAG_STOP_ON_BOL) != 0) {
								throw new LineUnderflowException();
							}
							m_out.write(BELL);
						}
						else
						{
							cursorpos--;
							m_out.write(ANSISequences.MOVE_CURSOR_LEFT_ONE);
						}
		    		}
		    		else
		    		{
		    			//Unknown escape sequence, do nothing
		    		}
		    	}
		    	else
		    	{
		    		//Unexpected character in sequence, terminate.
		    		escapemode = false;
		    	}
		    }
		    else
		    {
				switch(ch)
				{
					case ESC:
						escapemode = true;
						escapecode = "";
						break;
					case '\r':
						editing = false;
						m_out.write('\r');					
						m_out.write('\n');
						break;
					case NEWLINE:
						// Skip
						//
						break;
					case DEL:
					case BS:
						if(cursorpos == 0)
						{
							if((flags & FLAG_STOP_ON_BOL) != 0)
								throw new LineUnderflowException();
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
								m_out.write(SPACE);
								m_out.write(ANSISequences.moveCursorLeft(buffer.length() - cursorpos + 1));
							}
						}	
						break;
					case CTRL_A:
						if(((flags & FLAG_ECHO) != 0) && cursorpos > 0)
						{
							//Advance cursor to beginning of line
							m_out.write(ANSISequences.moveCursorLeft(cursorpos));
							cursorpos = 0;
						}
						break;
					case CTRL_E:
						if(((flags & FLAG_ECHO) != 0) && cursorpos < buffer.length())
						{
							//Advance cursor to end of line
							m_out.write(ANSISequences.moveCursorRight(buffer.length() - cursorpos));
							cursorpos = buffer.length();
						}
						break;
					case CTRL_X:
					case CTRL_U:
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
							cursorpos = 0;						
							break;
						}
					case CTRL_W:
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
								m_out.write(ANSISequences.moveCursorLeft(cursorpos - i));
								
								//Print rest of buffer from i.
								m_out.write(buffer.substring(i));
								
								//Pad with spaces.
								PrintUtils.printRepeated(m_out, SPACE, cursorpos - i);
								
								//Move back cursor to i.
								m_out.write(ANSISequences.moveCursorLeft(buffer.length() + (cursorpos - i) - i));
								
								//Set new cursorpos
								cursorpos = i;
							}
							break;
						}
					default:	
						// A stopchar?
						//
						if(stopChars != null && stopChars.indexOf(ch) != -1)
						    throw new StopCharException(buffer.toString(), ch);
	
						// Are we exceeding max length?
						//
						if(maxLength != 0 && buffer.length() >= maxLength - 1)
						{
							// Break on overflow?
							//
							if((flags & FLAG_STOP_ON_EOL) != 0)
							{
								buffer.insert(cursorpos++, ch);
								throw new LineOverflowException(buffer.toString());
							}
							else
							{
								// Don't break. Just make noise and ignore keystroke
								//
								m_out.write(BELL);
								m_out.flush();
								break; 
							}
						}
						
						// Store the character
						//
						buffer.insert(cursorpos++, ch);
							
						if((flags & FLAG_ECHO) != 0) {
							m_out.write(ch);
							if (cursorpos < buffer.length())
							{
								m_out.write(buffer.substring(cursorpos));
								m_out.write(SPACE);
								m_out.write(ANSISequences.moveCursorLeft(buffer.length() - cursorpos + 1));
							}
						}
						else {
							m_out.write('*');
						}
						break;
				}
		    }
			m_out.flush();
		}		
		return buffer.toString();
	}
	
	public void shutdown()
	{
		m_eventPoller.interrupt();
		m_keystrokePoller.interrupt();
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
		return (Event) m_eventQueue.removeFirst();
	}	
	
	// Implementation of NewlineListener
	//
	public void onNewline()
	{
	    try
	    {
		    if(m_dontCount)
		        return;
		    if(++m_lineCount >= m_tsProvider.getTerminalSettings().getHeight() - 1)
		    {
		        m_dontCount = true;
		        if(!this.getYesNo(m_morePrompt, m_formatter.format("misc.more.yeschars").toCharArray(), 
		                m_formatter.format("misc.more.nochars").toCharArray()))
		            throw new OutputInterruptedException();
		        m_lineCount = 0;
		    }
	    }
	    catch(InterruptedException e)
	    {
	        // Not much to do
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
