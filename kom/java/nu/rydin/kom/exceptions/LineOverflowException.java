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
public class LineOverflowException extends LineEditorException
{
	private String m_line;
	 
	public LineOverflowException()
	{
		super();
	}

	public LineOverflowException(String line)
	{
		m_line = line;
	}

	public String getLine()
	{
		return m_line;
	}
}
