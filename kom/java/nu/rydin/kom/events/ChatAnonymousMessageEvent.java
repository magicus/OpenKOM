/*
 * Created on Jun 10, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.events;

/**
 * @author uffe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChatAnonymousMessageEvent extends SingleUserEvent
{
	private final String m_message;
	
	public ChatAnonymousMessageEvent(long targetUser, String message)
	{
		super(targetUser);
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
