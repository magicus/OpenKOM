/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * Base class of all KOM events.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Event
{
	/**
	 * The id of the user that sent the event, or -1 if the user could not 
	 * be determined.
	 */
	private long m_originatingUser;
	
	/**
	 * Creates
	 *
	 */
	public Event()
	{
		m_originatingUser = -1;
	}
	
	public Event(long user)
	{
		m_originatingUser = user;
	}
	
	/**
	 * Returns the id of the user that sent the event, or -1 if the user could not 
	 * be determined.
	 */
	public long getOriginatingUser()
	{
		return m_originatingUser;
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

	/**
	 * Returns <tt>true</tt> if this event should be sent back to the originator
	 * when broadcasted. By default, it won't.
	 */
	public boolean sendToSelf()
	{
		return false;	
	}
}
