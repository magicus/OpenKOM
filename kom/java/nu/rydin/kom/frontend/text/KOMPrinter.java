/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * User session output. We'd love to derive this from Writer, but the good
 * folks at Sun prvents us from doing so, since they made Writer a class 
 * instead of an interface.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KOMPrinter
{
	private PrintWriter m_writer;
	private final OutputStream m_out;
	private String m_charSet;

	public KOMPrinter(OutputStream out, String charSet)
	throws UnsupportedEncodingException
	{
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
	
	public PrintWriter toPrintWriter()
	{
		return m_writer;
	}

	public boolean checkError()
	{
		return m_writer.checkError();
	}

	public void close() 
	throws IOException
	{
		m_writer.close();
	}

	public boolean equals(Object obj)
	{
		return m_writer.equals(obj);
	}

	public void flush() 
	throws IOException
	{
		m_writer.flush();
	}

	public int hashCode()
	{
		return m_writer.hashCode();
	}

	public void print(boolean b)
	{
		m_writer.print(b);
	}

	public void print(char c)
	{
		m_writer.print(c);
	}

	public void print(char[] s)
	{
		m_writer.print(s);
	}

	public void print(double d)
	{
		m_writer.print(d);
	}

	public void print(float f)
	{
		m_writer.print(f);
	}

	public void print(int i)
	{
		m_writer.print(i);
	}

	public void print(Object obj)
	{
		m_writer.print(obj);
	}

	public void print(String s)
	{
		m_writer.print(s);
	}

	public void print(long l)
	{
		m_writer.print(l);
	}

	public void println()
	{
		m_writer.println();
	}

	public void println(boolean x)
	{
		m_writer.println(x);
	}

	public void println(char x)
	{
		m_writer.println(x);
	}

	public void println(char[] x)
	{
		m_writer.println(x);
	}

	public void println(double x)
	{
		m_writer.println(x);
	}

	public void println(float x)
	{
		m_writer.println(x);
	}

	public void println(int x)
	{
		m_writer.println(x);
	}

	public void println(Object x)
	{
		m_writer.println(x);
	}

	public void println(String x)
	{
		m_writer.println(x);
	}

	public void println(long x)
	{
		m_writer.println(x);
	}

	public String toString()
	{
		return m_writer.toString();
	}

	public void write(char[] cbuf) 
	{
		m_writer.write(cbuf);
	}

	public void write(char[] cbuf, int off, int len) 
	{
		m_writer.write(cbuf, off, len);
	}

	public void write(int c) 
	{
		m_writer.write(c);
	}

	public void write(String str) 
	{
		m_writer.write(str);
	}

	public void write(String str, int off, int len) 
	{
		m_writer.write(str, off, len);
	}
}
