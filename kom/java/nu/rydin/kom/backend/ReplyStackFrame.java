/*
 * Created on Oct 26, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ReplyStackFrame
{
	private long[] m_replies;
	
	private int m_idx;
	
	private ReplyStackFrame m_next;
	
	public ReplyStackFrame(long[] replies, ReplyStackFrame next)
	{
		m_replies = replies;
		m_next = next;
		m_idx = 0;
	}
	
	public long pop()
	{
		return m_idx < m_replies.length ? m_replies[m_idx++] : -1;
	}
	
	public long peek()
	{
		return m_idx < m_replies.length ? m_replies[m_idx] : -1;
	}
	
	public boolean hasMore()
	{
		return m_idx < m_replies.length;
	}
	
	public ReplyStackFrame next()
	{
		return m_next;
	}
}
