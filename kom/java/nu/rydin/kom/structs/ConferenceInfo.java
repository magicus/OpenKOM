/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.sql.Timestamp;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ConferenceInfo extends NamedObject
{
    static final long serialVersionUID = 2005;
    
	private final long m_administrator;
	private final int m_permissions;
	private final int m_nonmemberPermissions;
	private final short m_visibility;
	private final long m_replyConf;
	private final int m_firstMessage;
	private final int m_lastMessage;
	private final Timestamp m_created;
	private final Timestamp m_lasttext;
	
	public ConferenceInfo(long id, Name name, String keywords, String emailAlias, long administrator, int permissions, 
	        int nonmemberPermissions, short visibility, long replyConf,
			Timestamp created, Timestamp lasttext, int firstMessage, int lastMessage )
	{
		super(id, name, keywords, emailAlias);
		m_administrator			= administrator;
		m_permissions 			= permissions;
		m_nonmemberPermissions 	= nonmemberPermissions;
		m_visibility			= visibility;
		m_replyConf				= replyConf;
		m_firstMessage 			= firstMessage;
		m_lastMessage 			= lastMessage;
		m_created				= created;
		m_lasttext				= lasttext;
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
	
	public int getNonmemberPermissions()
	{
	    return m_nonmemberPermissions;
	}
	
	public short getVisibility()
	{
	    return m_visibility;
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
