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
	
	public BroadcastAnonymousMessageEvent(String message)
	{
		m_message	= message;
	}
	
	public void dispatch(EventTarget target)
	{
		target.onEvent((BroadcastAnonymousMessageEvent) this);
	}
	
	public String getMessage()
	{
		return m_message;
	}
}
