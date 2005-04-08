/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class StopCharException extends LineEditorException
{
	private char m_stopChar;
	
	public StopCharException(String line, int pos, char stopChar)
	{
		super(line, pos);
		m_stopChar 	= stopChar;
	}

	public char getStopChar()
	{
		return m_stopChar;
	}
}
