/*
 * Created on May 30, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UserAttendanceEvent extends Event
{
	public static final int LOGIN			= 0;
	public static final int LOGOUT			= 1;
	public static final int FELL_ASLEEP		= 2;
	public static final int AWOKE			= 3;
	
	private final String m_userName;
	
	private final int m_type;

	public UserAttendanceEvent(long user, String userName, int type)
	{
		super(user);
		m_userName 	= userName;
		m_type 		= type;
	}
	
	public void dispatch(EventTarget target)
	{
		target.onEvent((UserAttendanceEvent) this);
	}
	
	public String getUserName()
	{
		return m_userName;
	}
	
	public int getType()
	{
		return m_type;
	}
}
