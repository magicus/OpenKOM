/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.sql.Timestamp;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ConferenceInfo extends NamedObject
{
	private long m_administrator;
	private int m_permissions;
	private long m_replyConf;
	private int m_firstMessage;
	private int m_lastMessage;
	private Timestamp m_created;
	private Timestamp m_lasttext;
	
	public ConferenceInfo(long id, String name, long administrator, int permissions, long replyConf,
			Timestamp created, Timestamp lasttext, int firstMessage, int lastMessage )
	{
		super(id, name);
		m_administrator	= administrator;
		m_permissions 	= permissions;
		m_replyConf		= replyConf;
		m_firstMessage 	= firstMessage;
		m_lastMessage 	= lastMessage;
		m_created		= created;
		m_lasttext		= lasttext;
	}
	
	public long getAdministrator()
	{
		return m_administrator;
	}

	public int getFirstMessage()
	{
		return m_firstMessage;
	}
	
	public long getReplyConf()
	{
		return m_replyConf;
	}

	public int getPermissions()
	{
		return m_permissions;
	}

	public int getLastMessage()
	{
		return m_lastMessage;
	}
	
	public Timestamp getCreated()
	{
		return m_created;
	}
	
	public Timestamp getLasttext()
	{
		return m_lasttext;
	}
}
