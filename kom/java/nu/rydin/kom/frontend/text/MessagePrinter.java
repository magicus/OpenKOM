/*
 * Created on Oct 16, 2003
 *  
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.structs.Envelope;

/**
 * Interfaces to classes capable of formatting and printing 
 * messages from a conference.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface MessagePrinter
{
	public void printMessage(Context context, Envelope envelope)
	throws KOMException;
}
