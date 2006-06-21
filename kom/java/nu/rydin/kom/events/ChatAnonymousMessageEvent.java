/*
 * Created on Jun 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * @author uffe
 */
public class ChatAnonymousMessageEvent extends SingleUserEvent
{
	private final String m_message;
	
	public ChatAnonymousMessageEvent(long targetUser, String message)
	{
		super(-1, targetUser);
		m_message 	= message;
	}
	
	/**
	 * Return the message body
	 */
	public String getMessage()
	{
		return m_message;
	}
	
	/**
	 * Dispatches an event to a target.
	 * 
	 * @param target The target
	 */
	public void dispatch(EventTarget target)
	{
		target.onEvent(this);
	}
}
