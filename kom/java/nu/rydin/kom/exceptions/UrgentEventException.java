/*
 * Created on Nov 12, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

import nu.rydin.kom.events.Event;

/**
 * This exception does not signal an error. Instead, it us used as a notification
 * of a high-priority event, such as system or session shutdown.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class UrgentEventException extends EventDeliveredException
{
    static final long serialVersionUID = 2005;
    
	public UrgentEventException(Event event, String line, int pos)
	{
		super(event, line, pos);
	}
}
