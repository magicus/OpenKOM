/*
 * Created on Sep 2, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import nu.rydin.kom.events.Event;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface EventSource
{
	/**
	 * Polls for an event. 
	 * 
	 * @param timeoutMs Timeout, in milliseconds. If no event could be
	 * delivered within this timeframe, <tt>null</tt> is returned.
	 */
	public Event pollEvent(int timeoutMs)
	throws InterruptedException;
}
