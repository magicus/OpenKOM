/*
 * Created on Oct 25, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.data.MembershipManager;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MembershipInfo;
import nu.rydin.kom.structs.MessageRange;
import nu.rydin.kom.structs.MessageRangeList;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class MembershipList
{
	/**
	 * Memberships keyed by conference
	 */
	private Map m_conferenceTable = new HashMap();
	
	/**
	 * Memberships in the order they are prioritized
	 */
	private MembershipInfo[] m_order;
	
	/**
	 * Memberships where the list of read messages has to be saved
	 */
	private Set m_dirty = new HashSet(); 
	
	/**
	 * Creates a <tt>MembershipList</tt> based on an array or <tt>MembershipInfo</rr>
	 * @param memberships
	 */
	public MembershipList(MembershipInfo[] memberships)
	{
		int top = memberships.length;
		m_order = new MembershipInfo[top];
		for(int idx = 0; idx < top; ++idx)
		{
			MembershipInfo each = memberships[idx];
			long conf = each.getConference();
			m_order[idx] = each;
			m_conferenceTable.put(new Long(conf), each);
		}
	}
	
	public MembershipInfo getOrNull(long conference)
	throws ObjectNotFoundException
	{
		return (MembershipInfo) m_conferenceTable.get(new Long(conference));
	}

	
	public MembershipInfo get(long conference)
	throws ObjectNotFoundException
	{
		MembershipInfo mi = this.getOrNull(conference);
		if(mi == null)
			throw new ObjectNotFoundException("Membership conference=" + conference);
		return mi;
	}
	
	public void markAsRead(long conference, int localnum)
	throws ObjectNotFoundException
	{
		MembershipInfo mi = (MembershipInfo) m_conferenceTable.get(new Long(conference));
		if(mi == null)
		    return; // We're not members, so we don't care!

		// Update ranges and mark as dirty
		//
		MessageRangeList l = mi.getReadMessages(); 
		mi.setReadMessages(l == null ? new MessageRangeList(new MessageRange(localnum, localnum)) : l.add(localnum));
		m_dirty.add(mi);
	}
	
	public void markAsUnread(long conference, int localnum)
	throws ObjectNotFoundException
	{
		MembershipInfo mi = (MembershipInfo) m_conferenceTable.get(new Long(conference));
		if(mi == null)
		    return; // We're not members, so we don't care!

		// Update ranges and mark as dirty
		//
		MessageRangeList l = mi.getReadMessages(); 
		mi.setReadMessages(l == null ? null : l.subtract(localnum));
		m_dirty.add(mi);
	}
	
	
	public boolean markAsReadEx(long conference, int localnum)
	throws ObjectNotFoundException
	{
		if (!this.isUnread(conference, localnum))
		{
			return false;
		}
		else
		{
			this.markAsRead(conference, localnum);
			return true;
		}
	}
	
	public synchronized int countUnread(long conference, ConferenceManager cm)
	throws ObjectNotFoundException, SQLException
	{
		ConferenceInfo ci = cm.loadConference(conference);
		MessageRange total = new MessageRange(ci.getFirstMessage(), ci.getLastMessage());
		MembershipInfo mi = (MembershipInfo) m_conferenceTable.get(new Long(conference));
		
		// Check that the list of unread messages is fully contained 
		// in the list of existing messages. If not, adjust!
		//
		MessageRangeList read = mi.getReadMessages();
		if(read != null && !read.containedIn(total))
		{
			// Ooops... List of read messages is not contained by the list of
			// what we think are existing messages. Someone has deleted a message
			// and we need to adjust the list of read messages
			//
			read = read.intersect(total);
			mi.setReadMessages(read);
			m_dirty.add(mi);
		}
		int answer = total.countOverlapping(read);
		return answer;
	}
	
	public int getNextMessageInConference(long confId, ConferenceManager cm)
	throws ObjectNotFoundException, SQLException
	{
		MembershipInfo mi = this.get(confId);
		ConferenceInfo ci = cm.loadConference(confId);
		MessageRangeList mr = mi.getReadMessages();
		int min = ci.getFirstMessage();
		return mr == null ? (min != 0 ? min : -1) : mr.getFirstUnread(min, ci.getLastMessage());
	}
	
	public boolean isUnread(long confId, int num)
	throws ObjectNotFoundException
	{
		MembershipInfo mi = this.get(confId);
		MessageRangeList mr = mi.getReadMessages();
		return mr == null || !mr.includes(num);
	}
	
	public void changeRead(long confId, int low, int high)
	throws ObjectNotFoundException
	{
		MembershipInfo mi = this.get(confId);
		mi.setReadMessages(new MessageRangeList(new MessageRange(low, high)));
		m_dirty.add(mi);
	}

	public long getFirstConferenceWithUnreadMessages(ConferenceManager cm)
	throws SQLException
	{
	    return innerNextConferenceWithUnreadMessages(0, cm);
	}

	public long getNextConferenceWithUnreadMessages(long startId, ConferenceManager cm)
	throws SQLException
	{
		// Find the first conference. Linear search. Believe me: It's
		// fast enough in this case.
		//
		int top = m_order.length;
		
		// If the first conference was not found, "offset" will remain at zero,
		// meaining that we start looking from the very first conference we're 
		// a member of. This is useful when the first conference is deleted. 
		// Typically, when this happens, the search will commence at the user's
		// private mailbox.
		// 
		int offset = 0;
		for(int idx = 0; idx < top; ++idx)
		{
			if(m_order[idx].getConference() == startId)
			{
				offset = idx + 1;
				break;
			}
		}
		return innerNextConferenceWithUnreadMessages(offset, cm);
	}

	private long innerNextConferenceWithUnreadMessages(int startIndex, ConferenceManager cm)
	throws SQLException
	{
		// Find the first conference with unread messages
		//
	    int top = m_order.length;
		for(int idx = 0; idx < top; ++idx)
		{
			MembershipInfo each = m_order[(idx + startIndex) % top];
			long id = each.getConference();
			
			try
			{
				if(this.countUnread(id, cm) > 0)
					return id;
			}
			catch(ObjectNotFoundException e)
			{
				// This conference could not be found. It's probably been
				// deleted, so just continue searching.
			}
		}
		
		// Nothing found
		//
		return -1;
	}

	
	public void save(long userId, MembershipManager mm)
	throws SQLException
	{
		for(Iterator itor = m_dirty.iterator(); itor.hasNext();)
		{
			MembershipInfo each = (MembershipInfo) itor.next();
			try
			{
				mm.updateMarkers(userId, each.getConference(), each.getReadMessages());
			}
			catch(ObjectNotFoundException e)
			{
				// TODO: This probably means that the conference has
				// been deleted. Should we try to do anything intelligent here?
			}
			itor.remove();
		}
	}
	
	public MembershipInfo[] getMemberships()
	{
		return m_order;
	}
	
	public void printDebugInfo(PrintStream out)
	{
		int top = m_order.length;
		for(int idx = 0; idx < top; ++idx)
		{
			MembershipInfo each = m_order[idx];
			out.println("Conf: " + each.getConference());
			out.print("Read markers:");
			MessageRangeList markers = each.getReadMessages();
			MessageRangeList rl = markers; 
			while(rl != null)
			{
				MessageRange r = rl.getRange();
				out.print(r.getMin() + "-" + r.getMax());
				rl = (MessageRangeList) rl.next();
				if(rl != markers)
					out.print(", ");
				else
					break;
			}
			out.println();
			out.println();
		}
	}
}
