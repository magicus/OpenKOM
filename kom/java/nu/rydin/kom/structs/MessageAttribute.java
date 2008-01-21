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
    static final long serialVersionUID = 2005;
    
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
	
	public static Name parseUserNamePayload(String payload, short kind) {
        if (kind == MessageAttributes.NOCOMMENT || kind == MessageAttributes.ORIGINAL_DELETED || kind == MessageAttributes.MAIL_RECIPIENT)
        {
            int i = payload.indexOf(":");
            if (i > 0 && payload.length() > i)
                return new Name(payload.substring(i + 1), Visibilities.PUBLIC, NameManager.USER_KIND);
        }
        return new Name("", Visibilities.PUBLIC, NameManager.USER_KIND);
	    
	}

	public Name getUsername()
	{
	    return parseUserNamePayload(m_value, m_kind);
	}

	public static long parseUserIdPayload(String payload, short kind)
	{
        if (kind == MessageAttributes.NOCOMMENT || kind == MessageAttributes.ORIGINAL_DELETED || kind == MessageAttributes.MAIL_RECIPIENT)
        {
            try
            {
                int i = payload.indexOf(":");
                if (i > 0)
                    return Long.parseLong(payload.substring(0, i));
            } 
            catch (NumberFormatException e)
            {
                //Malformed payload...
            }
        }
        return -1;
	    
	}
	
	public long getUserId()
	{
	    return parseUserIdPayload(m_value, m_kind);
	}	
}