/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChatMessageEvent extends SingleUserEvent
{
	private final long m_userId;
	
	private final String m_user;
	
	private final String m_message;
	
	public ChatMessageEvent(long targetUser, long userId, String user, String message)
	{
		super(targetUser);
		m_userId 	= userId;
		m_user		= user;
		m_message 	= message;
	}
	
	/**
	 * Returns the id of the originating user
	 */
	public long getUserId()
	{
		return m_userId;
	}
	
	/**
	 * Returns the name of the originating user
	 */
	public String getUserName()
	{
		return m_user;
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
