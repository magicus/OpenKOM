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
    private static class Line
    {
        public String m_content;
        
        public boolean m_newline = false;
        
        public Line(String content)
        {
            m_content = content;
        }
    }
    
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
			m_lines.add(new Line(line));	    
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		for(Iterator itor = m_lines.iterator(); itor.hasNext();)
		{
			Line line = (Line) itor.next();
			String content = line.m_content;
			if(content.length() > 0)
			{
				buffer.append(content);
				if(!Character.isWhitespace(content.charAt(content.length() - 1)))
					buffer.append(line.m_newline ? '\n' : ' ');
			}
			else if(line.m_newline)
			    buffer.append('\n');
		}
		return buffer.toString();
	}
	
	public String get(int idx)
	{
		return ((Line) m_lines.get(idx)).m_content;
	}
		
	public void put(int idx, String buffer)
	{
		m_lines.set(idx, new Line(buffer));
	}
	
	public void setNewline(int idx, boolean nl)
	{
	    ((Line) m_lines.get(idx)).m_newline = nl;
	}
	
	public boolean isNewline(int idx)
	{
	    return ((Line) m_lines.get(idx)).m_newline;
	}
	
	public void insertBefore(int where, String buffer)
	{
		this.insertBefore(where, new Line(buffer));
	}
	
	public void insertBefore(int where, Line line)
	{
		int top = m_lines.size();
		if(where == top)
			m_lines.add(line);
		else
		{
			// We're adding before the end. A little tricker.
			//
			ArrayList newList = new ArrayList(top + top / 2);
			int idx;
			where = Math.min(where, m_lines.size());
			for(idx = 0; idx < where; ++idx)
				newList.add(m_lines.get(idx));
			newList.add(line);
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
		int top = m_lines.size();
		if(idx > top)
			throw new ArrayIndexOutOfBoundsException();
		if(idx == top)
			m_lines.add(new Line(buffer));
		else
		{
		    Line line = (Line) m_lines.get(idx);
		    if(line != null)
		        line.m_content = buffer;
		    else
		        m_lines.set(idx, new Line(buffer));
		}
	}
	
	public int size()
	{
		return m_lines.size();
	}
	
	public void add(String string)
	{
		m_lines.add(new Line(string));
	}
	
}
