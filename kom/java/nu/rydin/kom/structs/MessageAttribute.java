/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.sql.Timestamp;

import nu.rydin.kom.backend.data.MessageManager;

/**
 * @author Henrik Schröder
 *
 */
public class MessageAttribute implements Serializable
{
    private final long m_id;
	private final long m_message;
	private final short m_kind;
	private final Timestamp m_created;	
	private final String m_value;

	public MessageAttribute(long id, long message, short kind, Timestamp created, String value)
	{
	    m_id = id;
	    m_message = message;
	    m_kind = kind;
	    m_created = created;
	    m_value = value;
	}

    public long getId()
    {
        return m_id;
    }
    
    public long getMessage() 
    {
        return m_message;
    }
	
    public short getKind() 
    {
        return m_kind;
    }
    
	public Timestamp getCreated() 
	{
        return m_created;
    }

	public String getValue() 
	{
        return m_value;
    }
	
	public static String constructUsernamePayload(long userId, String userName)
	{
	    return userId + ":" + userName; 
	}

	public String getUsername()
	{
	    if (m_kind == MessageManager.ATTR_NOCOMMENT || m_kind == MessageManager.ATTR_ORIGINAL_DELETED || m_kind == MessageManager.ATTR_MAIL_RECIPIENT)
	    {
	        int i = m_value.indexOf(":");
	        if (i > 0 && m_value.length() > i)
	            return m_value.substring(i + 1);
	    }
        return "";
	}
	
	public long getUserId()
	{
	    if (m_kind == MessageManager.ATTR_NOCOMMENT || m_kind == MessageManager.ATTR_ORIGINAL_DELETED || m_kind == MessageManager.ATTR_MAIL_RECIPIENT)
	    {
	        try
            {
	            int i = m_value.indexOf(":");
	            if (i > 0)
	                return Long.parseLong(m_value.substring(0, i));
            } 
	        catch (NumberFormatException e)
            {
	            //Malformed payload...
            }
	    }
	    return -1;
	}	
}