/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageRange implements Serializable
{
	private int m_min;
	
	private int m_max;
	
	public MessageRange(int min, int max)
	{
		m_min = min;
		m_max = max;
	}
	
	public int getMin()
	{
		return m_min;
	}
	
	public int getMax()
	{
		return m_max;
	}
	
	public MessageRangeList subtract(int num)
	{
		return this.subtract(new MessageRange(num, num));
	}
	
	public boolean includes(int message)
	{
		return m_min <= message && m_max >= message;
	}
	
	public MessageRangeList subtract(MessageRange range) 
	{
		// Prerequisite: "range" must be completely enclosed by us
		//
		int itsMin = range.getMin();
		int itsMax = range.getMax();

		// Case 1: Not overlapping at all. Range remains unchanged
		//		
		if(itsMax < m_min || itsMin > m_max)
			return new MessageRangeList(this);
		
		// Case 2: Completely overlapping. Nothing remains
		//
		if(itsMin <= m_min && itsMax >= m_max)
			return null;
			
		// Case 3: Overlapping at edges (i.e. overlapping with first or last element)
		//
		if(itsMin == m_min && itsMax == itsMin)
			return new MessageRangeList(new MessageRange(m_min + 1, m_max));
		if(itsMax == m_max && itsMax == itsMin)
			return new MessageRangeList(new MessageRange(m_min, m_max - 1));
			
		// Case 4: Partially overlapping between egdes
		//
		MessageRangeList l = new MessageRangeList(new MessageRange(m_min, itsMin - 1));
		new MessageRangeList(new MessageRange(itsMax + 1, m_max)).succeed(l);
		return l;
	}
	
	public MessageRangeList subtract(MessageRangeList ranges)
	{
		// Prerequisite: Ranges is sorted in ascending order. No overlaps.
		//
		if(ranges == null)
			return new MessageRangeList(this);
		MessageRangeList answer = null;
		MessageRange lhs = this;
		MessageRangeList each = ranges;
		do
		{
			MessageRange r = each.getRange();
			MessageRangeList result = lhs.subtract(r);
			
			// Nothing left? We're done here!
			//
			if(result == null)
				break;

			// Link in result
			//
			result.succeed(answer);
			answer = result;			
				
			// Continue processing upper portion
			//  
			each = (MessageRangeList) each.next();
			lhs = ((MessageRangeList) result.next()).getRange();
		} while(each != ranges);
		return answer;
	}
	
	public static MessageRangeList add(MessageRange r1, MessageRange r2)
	{
		// Make sure r1 <= r2
		//
		if(r1.getMin() > r2.getMin())
		{
			// Swap ranges
			//
			MessageRange tmp = r1;
			r1 = r2;
			r2 = tmp;
		}
		//
		// Completely disjoined?
		//
		if(r1.getMax() < r2.getMin() || r1.getMin() > r2.getMax())
			return new MessageRangeList(r1, r2);
		
		// Overlapping? Extend to enclose both ranges!
		//
		return new MessageRangeList(new MessageRange(r1.getMin(), r2.getMax())); 
	}
	
	public MessageRange intersect(MessageRange r)
	{
		// Completely disjoined? No intersection
		//
		if(this.getMax() < r.getMin() || this.getMin() > r.getMax())
			return null;
		return new MessageRange(Math.max(this.getMin(), r.getMin()), Math.min(this.getMax(), r.getMax()));
	}
	
	public int countOverlapping(MessageRangeList ranges)
	{
		if(m_min == 0 && m_max == 0)
			return 0;
		int n = m_max - m_min + 1;
		if(ranges == null)
			return n;
		MessageRangeList each = ranges;
		do
		{
			MessageRange r = each.getRange();
			n -= r.getMax() - r.getMin() + 1;
			each = (MessageRangeList) each.next();
		} while(each != ranges);
		return n;
	}	
}
