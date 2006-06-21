/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User session output. 
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KOMWriter extends PrintWriter
{
	private PrintWriter m_writer;
	private final OutputStream m_out;
	private String m_charSet;
	//private int m_lineNo = 0;
	private static final String s_lineSeparator = "\r\n";
	private final List<NewlineListener> m_newlineListeners = new LinkedList<NewlineListener>();

	public KOMWriter(OutputStream out, String charSet)
	throws UnsupportedEncodingException
	{
		super(out);
		m_charSet = charSet;
		m_out = out;
		m_writer = new PrintWriter(new OutputStreamWriter(out, charSet));
	}
	
	public void setCharset(String charSet)
	throws UnsupportedEncodingException
	{
		m_writer.flush();
		m_writer = new PrintWriter(new OutputStreamWriter(m_out, charSet));
	}
	
	public String getCharset()
	{
		return m_charSet;
	}
	
	public void addNewlineListener(NewlineListener listener)
	{
	    synchronized(m_newlineListeners)
	    {
	        m_newlineListeners.add(listener);
	    }
	}
	
	public boolean checkError()
	{
		return m_writer.checkError();
	}

	public void close() 
	{
		m_writer.close();
	}

	public boolean equals(Object obj)
	{
		return m_writer.equals(obj);
	}

	public void flush() 
	{
		m_writer.flush();
	}

	public int hashCode()
	{
		return m_writer.hashCode();
	}

	public void print(boolean b)
	{
		this.print(b ? "true" : "false");
	}

	public void print(char c)
	{
		this.write((int) c);
	}

	public void print(char[] s)
	{
		this.write(s);
	}

	public void print(double d)
	{
		this.print(Double.toString(d));
	}

	public void print(float f)
	{
		this.print(Float.toString(f));
	}

	public void print(int i)
	{
		this.print(Integer.toString(i));
	}

	public void print(Object obj)
	{
		this.print(obj == null ? "null" : obj.toString());
	}

	public void print(String s)
	{
		this.write(s == null ? "null" : s);
	}

	public void print(long l)
	{
		this.print(Long.toString(l));
	}

	public void println()
	{
		this.print(s_lineSeparator);
	}

	public void println(boolean x)
	{
		this.print(x);
		this.println();
	}

	public void println(char x)
	{
		this.print(x);
		this.println();
	}

	public void println(char[] x)
	{
		this.print(x);
		this.println();
	}

	public void println(double x)
	{
		this.print(x);
		this.println();
	}

	public void println(float x)
	{
		this.print(x);
		this.println();
	}

	public void println(int x)
	{
		this.print(x);
		this.println();
	}

	public void println(Object x)
	{
		this.print(x);
		this.println();
	}

	public void println(String x)
	{
		this.print(x);
		this.println();
	}

	public void println(long x)
	{
		this.print(x);
		this.println();
	}

	public void write(char[] cbuf) 
	{
		this.write(cbuf, 0, cbuf.length);
	}

	public void write(char[] cbuf, int off, int len) 
	{
		len += off;
		for(int idx = off; idx < len; ++idx)
			this.write(cbuf[idx]);
	}

	public void write(int c) 
	{
		m_writer.write(c);
		
		// Notify newline listener if we're writing a newline
		//
		if(c == '\n')
		{
		    m_writer.flush();
		    synchronized(m_newlineListeners)
		    {
		        for(Iterator itor = m_newlineListeners.iterator(); itor.hasNext();)
		        {
		            ((NewlineListener) itor.next()).onNewline();
		        }
		    }
		} else if(c == '\r')
		{
		    //m_writer.flush();
		}
	}

	public void write(String str) 
	{
		this.write(str, 0, str.length());
	}

	public void write(String str, int off, int len) 
	{
		len += off;
		for(int idx = off; idx < len; ++idx)
			this.write(str.charAt(idx));
	}
}
