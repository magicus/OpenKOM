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
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.SessionShutdownEvent;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class LineEditor
{
	private static final char BELL 		= 7;
	private static final char BS		= 8;
	private static final char CTRL_U	= 21;
	private static final char CTRL_W	= 23;
	private static final char DEL		= 127;
	
	private final ReaderProxy m_in;
	
	private final InputStream m_inStream;
	
	private final KOMPrinter m_out;
	
	private final EventTarget m_target;
	
	private ServerSession m_session;
	
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
	
		
	public LineEditor(InputStream in, KOMPrinter out, EventTarget target, ServerSession session, String charset)
	throws UnsupportedEncodingException
	{
		m_in		= new ReaderProxy(new InputStreamReader(in, charset));
		m_inStream  = in;
		m_out		= out;
		m_target	= target;
		
		// Start pollers
		//
		if(session != null)
			this.setSession(session);
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
		return innerReadLine(null, true, true);
	}
	
	public String readLineStopOnEvent(String defaultString)
	throws IOException, InterruptedException, EventDeliveredException
	{
		return innerReadLine(defaultString, true, true);
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
			return innerReadLine(null, false, false);
		}
		catch(EventDeliveredException e)
		{
			// HUH??!?!? We asked NOT TO have events interrupt us, but
			// still here we are. Something is broken!
			//
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
			return innerReadLine(defaultString, true, false);
		}
		catch(EventDeliveredException e)
		{
			// HUH??!?!? We asked NOT TO have events interrupt us, but
			// still here we are. Something is broken!
			//
			throw new RuntimeException("This should not happen!", e);
		}
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
	
	protected String innerReadLine(String defaultString, boolean echo, boolean stopOnEvent)
	throws EventDeliveredException, IOException, InterruptedException
	{
		StringBuffer buffer = new StringBuffer(500);
		int idx = 0;
		
		// Did we get a default string?
		//
		if(defaultString != null)
		{
			buffer.append(defaultString);
			m_out.print(defaultString);
			m_out.flush();
			idx = defaultString.length();
		}
				
		boolean editing = true;
		while(editing)
		{
			// Read next event from queue
			//
			Event e = this.getNextEvent();
			
			// Not a keystroke? If we already have keystrokes in the buffer
			// we should not handle the event now, but wait until we're done
			// editing.
			//
			if(!(e instanceof KeystrokeEvent))
			{
				// IOException while reading user input? Pass it on!
				//
				if(e instanceof IOExceptionEvent)
					throw ((IOExceptionEvent) e).getException();
				
				// Session shutdown? Get us out of here immediately!
				//
				if(e instanceof SessionShutdownEvent)
					throw new InterruptedException();
				e.dispatch(m_target);
				if(stopOnEvent && buffer.length() == 0)
					throw new EventDeliveredException(e);
				
				// Done with this event!
				//
				continue;
			}
			
			// Getting here means that we have a KeyStrokeEvent. Handle it!
			//
			char ch = ((KeystrokeEvent) e).getChar();
			switch(ch)
			{
				case DEL:
				case BS:
					if(idx == 0)
						m_out.write(BELL);
					else
					{
						buffer.deleteCharAt(--idx);
						m_out.write(8);
						m_out.write(32);
						m_out.write(8);
					}	
					break;
				case '\r':
					editing = false;
					m_out.write('\n');
					m_out.write('\r');					
					break;
				case CTRL_U:
					{
						int top = buffer.length();
						
						// Advance cursor to end of line
						//
						int n = top  - idx;
						while(n-- > 0)
							m_out.write(' ');
						
						// Delete to beginning of line
						//
						n = top;
						while(n-- > 0)
							m_out.write("\b \b");
						buffer.delete(0, buffer.length());
						idx = 0;						
						break;
					}
				case CTRL_W:
					{
						int p = idx;
						for(; idx > 0 && Character.isSpaceChar(buffer.charAt(idx - 1)); --idx)
							m_out.write("\b \b");
						for(; idx > 0 && !Character.isSpaceChar(buffer.charAt(idx - 1)); --idx)
							m_out.write("\b \b");
						buffer.delete(idx, p);
						break;
					}
				case '\n':
					// Skip
					//
					break;
				default:
					buffer.insert(idx++, ch);
					if(echo)
						m_out.write(ch);
					else
						m_out.write('*');
					break;
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
}