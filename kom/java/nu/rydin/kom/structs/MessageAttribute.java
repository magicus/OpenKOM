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
	private final long m_message;
	private final short m_kind;
	private final Timestamp m_created;	
	private final String m_value;

	public MessageAttribute(long message, short kind, Timestamp created, String value)
	{
	    m_message = message;
	    m_kind = kind;
	    m_created = created;
	    m_value = value;
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
	
	//TODO (skrolle) FIX THIS TEMPORARY SHIT!
	public static String constructNoCommentPayload(String username)
	{
	    return username;
	}

	//TODO (skrolle) FIX THIS TEMPORARY SHIT!
	public String getNoCommentUsername()
	{
	    if (m_kind == MessageManager.ATTR_NOCOMMENT)
	    {
	        return m_value;
	    }
	    else
	    {
	        return m_value;
	    }
	}
}