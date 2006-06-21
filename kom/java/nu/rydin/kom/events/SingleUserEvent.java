/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * Base-class for events intended for a single user.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class SingleUserEvent extends Event
{
	/**
	 * The user this event is intended for
	 */
	private final long m_targetUser;
	
	/**
	 * Creates an event
	 * @param targetUser The user this event is intended for
	 */
	public SingleUserEvent(long originatingUser, long targetUser)
	{
        super(originatingUser);
		m_targetUser = targetUser;
	}
	
	/**
	 * Returns the id of the user this event is intended for
	 */
	public long getTargetUser()
	{
		return m_targetUser;
	}
}
