/*
 * Created on Oct 26, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

import com.frameworx.util.ListAtom;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageRangeList extends ListAtom implements Serializable
{
	private MessageRange m_range;
	
	public MessageRangeList(MessageRange range)
	{
		super();
		m_range = range;
	}
	
	public MessageRangeList(MessageRange range1, MessageRange range2)
	{
		super();
		m_range = range1;
		new MessageRangeList(range2).succeed(this);
	}	
	
	public MessageRange getRange()
	{
		return m_range;		
	}
	
	public MessageRangeList add(int message)
	{
		// Prerequisite: "ranges" is sorted, no overlaps
		//
		boolean first = true;
		MessageRangeList each = this;
		do
		{
			MessageRange r = each.getRange();
			
			// Already in a range? No need to do anything.
			//
			if(r.includes(message))
				return this;
				
			int min = r.getMin();
			int max = r.getMax();

			// Immediately before or after a range? Extend the range!
			//			
			if(message == min - 1)
			{
				each.m_range = new MessageRange(message, r.getMax());
				return this;
			}
			if(message == max + 1)
			{
				// Do we become adjecent to our successor? Merge!
				//
				MessageRangeList nl = (MessageRangeList) each.next();  
				MessageRange nr = nl.getRange();
				if(nl != this && nr.getMin() - 1 == message)
				{
					// Merge ranges!
					//
					each.m_range = new MessageRange(min, nr.getMax());
					nl.yank();
				}
				else
					each.m_range = new MessageRange(r.getMin(), message);
				return this;
			}
			
			// Between this and the previous range? Vojne (as we say in Sweden)!
			// We have to insert a new range (containing just this message)
			//
			if(message < min)
			{				
				// We're lower than the current range and we have not yet been 
				// included in a range. That means we have to create our own 
				// independently ruled banana-republic of a range. 
				//
				MessageRangeList here = (MessageRangeList) new MessageRangeList(
					new MessageRange(message, message)).precede(each);
				return first ?  here : this;
			}
			first = false;
			each = (MessageRangeList) each.next();
		} while(each != this);
		
		// Getting here can only mean one thing: We we not extending or overlapping
		// any ranges and we did not fit between two ranges. So what happened then?
		// *drumroll* We should form our own message range after the last one!
		//
		// Since we've traversed the whole list, "each" now points at the first
		// element. Since the list is circular, prepending an element to the first 
		// one is the same as appending it to the last one.
		//
		new MessageRangeList(new MessageRange(message, message)).precede(each);
		return this;
	}
	
	public int getFirstUnread(int min, int max)
	{
		// The first message is below the first chunk of unread messages?
		// Then that's the first unread!
		// 
		if(min < m_range.getMin())
			return min;
		return m_range.getMax() < max ? m_range.getMax() + 1 : -1;
	}
	
	public boolean includes(int num)
	{
		MessageRangeList each = this;
		do
		{
			MessageRange r = each.getRange();
			
			// Any point in looking any further?
			//
			if(r.getMin() > num)
				return false;
			if(r.includes(num))
				return true;
			each = (MessageRangeList) each.next();
		} while(each != this);
		
		// Nothing found
		//
		return false;
	}
}