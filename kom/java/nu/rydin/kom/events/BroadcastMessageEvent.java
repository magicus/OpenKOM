/*
 * Created on Nov 12, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

import nu.rydin.kom.structs.Name;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class BroadcastMessageEvent extends Event
{
	private final Name m_userName;
	
	private final String m_message;
	
	private final long m_logId;
	
	private final short m_kind;
	
	public BroadcastMessageEvent(long user, Name userName, String message, long logId, short kind)
	{
		super(user);
		m_userName 	= userName;
		m_message	= message;
		m_logId		= logId;
		m_kind		= kind;
	}
	
	public void dispatch(EventTarget target)
	{
		target.onEvent(this);
	}
	
	public Name getUserName()
	{
		return m_userName;
	}
	
	public String getMessage()
	{
		return m_message;
	}
	
	public long getLogId()
	{
	    return m_logId;
	}
	
	public long getKind()
	{
	    return m_kind;
	}
}
