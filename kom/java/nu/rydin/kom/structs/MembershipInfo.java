/*
 * Created on Oct 12, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;
import java.util.StringTokenizer;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MembershipInfo implements Serializable
{
    static final long serialVersionUID = 2005;
    
	private final long m_user;
	private final long m_conference;
	private final boolean m_active;
	private final long m_priority;
	private final long m_flags;
	private final int m_permissions;
	private final int m_negationMask;
	private MessageRangeList m_readMessages;
	
	public MembershipInfo(long user, long conference, boolean active, long priority, long flags, 
		MessageRangeList readMessages, int permissions, int negationMask)
	{
		m_user 			= user;
		m_conference 	= conference;
		m_active		= active;
		m_priority 		= priority;
		m_flags 		= flags;
		m_readMessages 	= readMessages;
		m_permissions	= permissions;
		m_negationMask	= negationMask;
	}
	
	public MembershipInfo(long user, long conference, boolean active, long priority, long flags, 
		String readMessages, int permissions, int negationMask)
	{
		this(user, conference, active, priority, flags, decodeMessageRanges(readMessages), permissions, negationMask);
	}

	public long getConference()
	{
		return m_conference;
	}
	
	public boolean isActive()
	{
		return m_active;
	}

	public long getFlags()
	{
		return m_flags;
	}

	public long getPriority()
	{
		return m_priority;
	}
	
	/*
	public int getPermissions()
	{
		return m_permissions;
	}
	
	public int getNegationMask()
	{
		return m_negationMask;
	}*/

	public MessageRangeList getReadMessages()
	{
		return m_readMessages;
	}
	
	public void setReadMessages(MessageRangeList readMessages)
	{
		m_readMessages = readMessages; 
	}

	public long getUser()
	{
		return m_user;
	}
	
	public int getPermissions()
	{
	    return m_permissions;
	}
	
	public int getNegationMask()
	{
	    return m_negationMask;
	}
	
	public static MessageRangeList decodeMessageRanges(String messageRanges)
	{
		if(messageRanges == null)
			return null;
		MessageRangeList list = null;
		StringTokenizer st = new StringTokenizer(messageRanges, ",");
		while(st.hasMoreElements())
		{
			String s = st.nextToken();
			int p = s.indexOf('-');
			if(p == -1)
				throw new RuntimeException("Syntax error in range: " + s);
			MessageRangeList each = new MessageRangeList(new MessageRange(
				Integer.parseInt(s.substring(0, p)),
				Integer.parseInt(s.substring(p + 1))));
			if(list != null)
				each.succeed(list);
			list = each;
		}
		
		// The list is circular, so returning "next" here gives us the fist
		//
		return (MessageRangeList) list.next();
	}
	
	public static String encodeMessageRanges(MessageRangeList ranges)
	{
		if(ranges == null)
			return null;
		StringBuffer sb = new StringBuffer();
		MessageRangeList each = ranges;
		for(;;)
		{
			MessageRange r = each.getRange();
			sb.append(r.getMin());
			sb.append('-');
			sb.append(r.getMax());
			
			// Getting back to the start here means that we
			// have traversed the entire circular list.
			//
			each = (MessageRangeList) each.next();
			if(each != ranges)
				sb.append(',');
			else
				break;
		}
	return sb.toString(); 
	}
}
