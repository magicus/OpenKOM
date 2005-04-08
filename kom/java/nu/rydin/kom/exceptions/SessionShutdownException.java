/*
 * Created on Nov 12, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

import nu.rydin.kom.events.Event;

/**
 * This exception does not signal an error. A session receiving
 * this exception should immedately clean up and terminate itself.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SessionShutdownException extends UrgentEventException
{
	public SessionShutdownException(Event e, String line, int pos)
	{
		super(e, line, pos);
	}
}
