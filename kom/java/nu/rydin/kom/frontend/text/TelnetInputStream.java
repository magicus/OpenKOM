/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TelnetInputStream extends InputStream
{
	private static final short OFF 		= 0;
	private static final short ON 		= 1;
	private static final short HIDDEN 	= 2;
	
	private static final short STATE_NORMAL 		= 0;
	private static final short STATE_IAC 			= 1;
	private static final short STATE_WILL 			= 2;
	private static final short STATE_DO 			= 3;
	private static final short STATE_WONT 			= 4;
	private static final short STATE_DONT 			= 5;
	private static final short STATE_SB				= 6;
	private static final short STATE_DATA			= 7;
	private static final short STATE_AFTER_COMMAND	= 8;
	
	private static final byte CHAR_SE		= -16;
	private static final byte CHAR_SB		= -6;
	private static final byte CHAR_WILL		= -5;
	private static final byte CHAR_WONT		= -4;
	private static final byte CHAR_DO		= -3;
	private static final byte CHAR_DONT		= -2;
	private static final byte CHAR_IAC		= -1;
	
	private static final int OPT_ECHO		= 1;
	private static final int OPT_SUPPRESS_GA= 3;
	private static final int OPT_NAOCRD		= 10;
	private static final int OPT_FLOWCONTROL = 33;
	private static final int OPT_NAWS		= 31;
	private static final int OPT_LINEMODE	= 34;
	private static final int OPT_ENVIRON	= 39;
	
	private short m_state = STATE_NORMAL;
	
	private boolean m_suppressProcessing = false;
	
	private InputStream m_input;
	
	private OutputStream m_output;
	
	private int[] m_dataBuffer;
	
	private int m_dataState;
	
	private int m_dataIdx;
	
	private List m_sizeListeners = new LinkedList();
	
	public TelnetInputStream(InputStream input, OutputStream output)
	throws IOException
	{
		m_input 	= input;
		m_output 	= output; 
		
		// We will echo
		//
		this.sendOption(CHAR_WILL, OPT_ECHO);
		
		// Please don't use linemode
		//
		this.sendOption(CHAR_DONT, OPT_LINEMODE);
		
		// Please use NAWS if you support it!
		//
		this.sendOption(CHAR_DO, OPT_NAWS);
		
		m_output.flush();
	}
	
	public void addSizeListener(TerminalSizeListener listener)
	{
		synchronized(m_sizeListeners)
		{
			m_sizeListeners.add(listener);
		}
	}
	
	public int read() 
	throws IOException
	{
		for(;;)
		{
			int data = m_input.read();
			if(data == -1)
				return data; // EOF!
			
			// Don't echo if we're called from block-read methods, since they
			// will take care of echoing themselves.
			//
			if(!m_suppressProcessing)
				return data;
			boolean valid = this.stateMachine(data);
			if(valid) 
				return data;
		}
	}
	
	public int read(byte b[]) throws IOException 
	{
		return this.read(b, 0, b.length);
	}

	public int read(byte b[], int off, int length) 
	throws IOException 
	{
		byte[] buffer = new byte[length];
		for(;;)
		{
			int n = -1;
			try
			{
				n = m_input.read(buffer);
			}
			catch(SocketException e)
			{
				// The socket was probably disconnected. Treat as EOF.
				//
				return -1;
			}
			
			// End of file. Nothing more to do!
			//
			if(n == -1)
				return -1;
			n = this.handleBuffer(buffer, off, n);
			
			// Anything left after trimming?
			// If not, try reading again
			//
			if(n == 0)
				continue;
			System.arraycopy(buffer, 0, b, off, n);
			return n;
		}
	}
		
	protected int handleBuffer(byte[] bytes, int offset, int length)
	throws IOException
	{
		byte[] copy = new byte[length];
		int n = 0;
		for(int idx = 0; idx < length; ++idx)
		{
			byte b = bytes[offset + idx];
			boolean valid = this.stateMachine(b);
			if(valid)
				copy[n++] = b;
		}
		System.arraycopy(copy, 0, bytes, offset, n);
		return n;
	}
	
	protected void sendCommand(int command)
	throws IOException
	{
		m_output.write(CHAR_IAC);
		m_output.write(command);
	}
	
	protected void sendOption(int verb, int option)
	throws IOException
	{
		this.sendCommand(verb);
		m_output.write(option);
	}
	
	
	protected boolean stateMachine(int b)
	throws IOException
	{
		//System.out.println("Char: "+ b);
		switch(m_state)
		{
			case STATE_NORMAL:
				if(b == CHAR_IAC)
				{
					m_state = STATE_IAC;
					break;
				}
				// Strip EOR
				//
				if(b == 0)
					break;	
				return true;								
			case STATE_IAC:
				switch(b)
				{
					case CHAR_IAC:
						// Escaped 255
						//
						m_state = STATE_NORMAL;
						return true;
					case CHAR_WILL:
						m_state = STATE_WILL;
						break;
					case CHAR_WONT: 
						m_state = STATE_WONT; 
						break;
					case CHAR_DO:
						m_state = STATE_DO;
						break;
					case CHAR_DONT:
						m_state = STATE_DONT;
						break;
					case CHAR_SB:
						m_state = STATE_SB;
						break;
					default: 
						this.handleCommand(b);
						m_state = STATE_NORMAL;
						break;
				}
				break;
			case STATE_WILL:
				this.handleWill(b);
				m_state = STATE_NORMAL;
				break;
			case STATE_WONT:
				this.handleWont(b);
				m_state = STATE_NORMAL;
				break;
			case STATE_DO:
				this.handleDo(b);
				m_state = STATE_NORMAL;
				break;
			case STATE_DONT:
				this.handleDont(b);
				m_state = STATE_NORMAL;
				break;
			case STATE_SB:
				switch(b)
				{
					case OPT_NAWS:
						m_dataBuffer = new int[4];
						m_dataIdx = 0;
						m_state = STATE_DATA;
						break;
				}
				m_dataState = b;
				break;
			case STATE_DATA:
				if(m_dataIdx < m_dataBuffer.length)
					m_dataBuffer[m_dataIdx++] = b;
				else if(b == CHAR_IAC)
					m_state = STATE_AFTER_COMMAND;
				else
					m_state = STATE_NORMAL; // Invalid command, go back to normal
				break;
			case STATE_AFTER_COMMAND:				
				if(b != CHAR_SE)
				{
					// Huh? Not end of subnegotiation? 
					//
					m_state = STATE_NORMAL;
					break;
				}
				// End of command
				//
				switch(m_dataState)
				{
					case OPT_NAWS:
						this.handleNaws();
						// Fall thru...
					default:
						m_state = STATE_NORMAL;
				}
			break;
		}
		return false;
	}
	
	protected void handleCommand(int ch)
	{
		System.out.println("Command: " +ch);
	}
	
	protected void handleWill(int ch)
	throws IOException
	{
		System.out.println("Will: " + ch);
		switch(ch)
		{
			case OPT_LINEMODE:
				this.sendOption(CHAR_DONT, OPT_LINEMODE);
				this.sendOption(CHAR_WONT, OPT_LINEMODE);
				break;
			case OPT_FLOWCONTROL:
				this.sendOption(CHAR_DONT, OPT_FLOWCONTROL);
				this.sendOption(CHAR_WONT, OPT_FLOWCONTROL);
				break;
			case OPT_ENVIRON:
				this.sendOption(CHAR_DONT, OPT_ENVIRON);
				this.sendOption(CHAR_WONT, OPT_ENVIRON);
				break;				
		}
	}
	
	protected void handleWont(int ch)
	{
		System.out.println("Won't: " + ch);
	}
	
	protected void handleDo(int ch)
	throws IOException
	{
		System.out.println("Do: " + ch);
		switch(ch)
		{
			case OPT_ECHO:
				// Yes, we will echo
				//
				this.sendOption(CHAR_WILL, OPT_ECHO);
				break;
			case OPT_SUPPRESS_GA:
				// Yes, we will supress GA
				//
				this.sendOption(CHAR_WILL, OPT_SUPPRESS_GA);
				break;
		}
	}
	
	protected void handleDont(int ch)
	throws IOException
	{
		System.out.println("Don't: " + ch);
		switch(ch)
		{
			case OPT_ECHO:
				// I'm sorry, but we will echo
				//
				this.sendOption(CHAR_WILL, OPT_ECHO);
				break;
			case OPT_SUPPRESS_GA:
				// I'm sorry, but we will supress GA.
				//
				this.sendOption(CHAR_WILL, OPT_SUPPRESS_GA);
				break;
		}

	}
	
	protected void handleNaws()
	{
		int width = (complement2(m_dataBuffer[0]) << 8) + complement2(m_dataBuffer[1]);
		int heigth = (complement2(m_dataBuffer[2]) << 8) + complement2(m_dataBuffer[3]);
		synchronized(m_sizeListeners)
		{
			for(Iterator itor = m_sizeListeners.iterator(); itor.hasNext();)
				((TerminalSizeListener) itor.next()).terminalSizeChanged(width, heigth);
		}
	}
	
	protected static int complement2(int n)
	{
		return n < 0 ? 256 - n : n;
	}
}
