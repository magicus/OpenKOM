/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import nu.rydin.kom.events.Event;

/**
 * Event carrying information about a typed character.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KeystrokeEvent extends Event
{
	private char m_ch;
	
	public KeystrokeEvent(char ch)
	{
		m_ch = ch;
	}
	
	/**
	 * Returns the typed character.
	 */
	public char getChar()
	{
		return m_ch;
	}
}
