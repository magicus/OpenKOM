/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.sql.Timestamp;

import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.constants.MessageAttributes;
import nu.rydin.kom.constants.Visibilities;

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

	public Name getUsername()
	{
	    if (m_kind == MessageAttributes.NOCOMMENT || m_kind == MessageAttributes.ORIGINAL_DELETED || m_kind == MessageAttributes.MAIL_RECIPIENT)
	    {
	        int i = m_value.indexOf(":");
	        if (i > 0 && m_value.length() > i)
	            return new Name(m_value.substring(i + 1), Visibilities.PUBLIC, NameManager.USER_KIND);
	    }
	    return new Name("", Visibilities.PUBLIC, NameManager.USER_KIND);
	}
	
	public long getUserId()
	{
	    if (m_kind == MessageAttributes.NOCOMMENT || m_kind == MessageAttributes.ORIGINAL_DELETED || m_kind == MessageAttributes.MAIL_RECIPIENT)
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