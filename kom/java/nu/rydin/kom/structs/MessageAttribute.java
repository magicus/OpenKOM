/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Henrik Schröder
 *
 */
public class MessageAttribute implements Serializable
{
	private final long m_message;
	private final int m_kind;
	private final Timestamp m_created;	
	private final String m_value;

	public MessageAttribute(long message, int kind, Timestamp created, String value)
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
	
    public int getKind() 
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
}