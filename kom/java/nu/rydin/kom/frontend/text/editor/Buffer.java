/*
 * Created on Jun 16, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Buffer
{
	private ArrayList m_lines = new ArrayList();
	
	public Buffer()
	{
	    super();
	}
	
	public Buffer(WordWrapper wrapper)
	{
	    this.fill(wrapper);
	}
	
	public void fill(WordWrapper wrapper)
	{
		String line;
		while((line = wrapper.nextLine()) != null)
			m_lines.add(new StringBuffer(line));	    
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		for(Iterator itor = m_lines.iterator(); itor.hasNext();)
		{
			StringBuffer line = (StringBuffer) itor.next();
			if(line.length() > 0)
			{
				buffer.append(line);
				if(!Character.isWhitespace(line.charAt(line.length() - 1)))
					buffer.append(' ');
			}
			else
			    buffer.append('\n');
		}
		return buffer.toString();
	}
	
	public StringBuffer get(int idx)
	{
		return (StringBuffer) m_lines.get(idx);
	}
	
	public void put(int idx, StringBuffer buffer)
	{
		m_lines.set(idx, buffer);
	}
	
	public void put(int idx, String buffer)
	{
		m_lines.set(idx, new StringBuffer(buffer));
	}
	
	public void insertBefore(int where, String buffer)
	{
		this.insertBefore(where, new StringBuffer(buffer));
	}
	
	public void insertBefore(int where, StringBuffer buffer)
	{
		int top = m_lines.size();
		if(where == top)
			m_lines.add(buffer);
		else
		{
			// We're adding before the end. A little tricker.
			//
			ArrayList newList = new ArrayList(top + top / 2);
			int idx;
			where = Math.min(where, m_lines.size());
			for(idx = 0; idx < where; ++idx)
				newList.add(m_lines.get(idx));
			newList.add(buffer);
			for(; idx < top; ++idx)
				newList.add(m_lines.get(idx));
			m_lines = newList;
		}
	}
	
	public void remove(int where)
	{
		int idx;
		int top = m_lines.size();
		ArrayList newList = new ArrayList(m_lines.size());
		for(idx = 0; idx < where; ++idx)
			newList.add(m_lines.get(idx));
		++idx;
		for(; idx < top; ++idx)
			newList.add(m_lines.get(idx));
		m_lines = newList;
	}
	
	public void set(int idx, String buffer)
	{
		this.set(idx, new StringBuffer(buffer));
	}
	
	public void set(int idx, StringBuffer buffer)
	{
		int top = m_lines.size();
		if(idx > top)
			throw new ArrayIndexOutOfBoundsException();
		if(idx == top)
			m_lines.add(buffer);
		else
			m_lines.set(idx, buffer);
	}
	
	public int size()
	{
		return m_lines.size();
	}
	
	public void add(String string)
	{
		m_lines.add(new StringBuffer(string));
	}
	
	public void add(StringBuffer buffer)
	{
		m_lines.add(buffer);
	}
}
