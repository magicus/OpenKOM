/*
 * Created on Nov 12, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

import nu.rydin.kom.events.Event;

/**
 * This exception does not signal an error. Instead, it is
 * used as a signal that the system is about to be shut down 
 * immediately and that the receiving thread should immediatly
 * clean up and terminate.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SystemShutdownException extends UrgentEventException
{
	public SystemShutdownException(Event e)
	{
		super(e);
	}
}
