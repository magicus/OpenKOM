/*
 * Created on Nov 12, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class BroadcastMessageEvent extends Event
{
	private final String m_userName;
	
	private final String m_message;
	
	public BroadcastMessageEvent(long user, String userName, String message)
	{
		super(user);
		m_userName 	= userName;
		m_message	= message;
	}
	
	public void dispatch(EventTarget target)
	{
		target.onEvent((BroadcastMessageEvent) this);
	}
	
	public String getUserName()
	{
		return m_userName;
	}
	
	public String getMessage()
	{
		return m_message;
	}
}
