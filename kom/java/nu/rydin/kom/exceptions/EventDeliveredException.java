/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

import nu.rydin.kom.events.*;

/**
 * This exception does not indicate an error, but is thrown when 
 * a high-priority event is delivered
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class EventDeliveredException extends LineEditorException
{
	private Event m_event;
    static final long serialVersionUID = 2005;
    	
	public EventDeliveredException(Event event, String line, int pos)
	{
	    super(line, pos);
		m_event = event;
	}
	
	public Event getEvent()
	{
		return m_event;
	}
}
