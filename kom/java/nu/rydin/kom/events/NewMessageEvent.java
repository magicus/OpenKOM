/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;

/**
 * Event send when a new message is available.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NewMessageEvent extends Event
{
	private long m_conferenceId;
	
	private int m_localNum;
	
	private long m_globalId;

	public NewMessageEvent(long originator, long conferenceId, int localNum, long globalId)
	{
		super(originator);
		m_conferenceId 	= conferenceId;
		m_localNum 		= localNum;
		m_globalId 		= globalId;  
	}
	
	public long getConference()
	{
		return m_conferenceId;
	}
	
	public int getLocalNum()
	{
		return m_localNum;
	}
	
	public long getGlobalId()
	{
		return m_globalId;
	}
	
	public void dispatch(EventTarget target)
	{
		target.onEvent(this);
	}

}
