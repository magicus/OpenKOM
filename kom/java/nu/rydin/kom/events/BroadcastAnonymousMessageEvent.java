/*
 * Created on Nov 12, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

public class BroadcastAnonymousMessageEvent extends Event
{
	private final String m_message;
	
	private final long m_logId;
	
	public BroadcastAnonymousMessageEvent(String message, long logId)
	{
		m_message	= message;
		m_logId		= logId;
	}
	
	public void dispatch(EventTarget target)
	{
		target.onEvent((BroadcastAnonymousMessageEvent) this);
	}
	
	public String getMessage()
	{
		return m_message;
	}
	
	public long getLogId()
	{
	    return m_logId;
	}
}
