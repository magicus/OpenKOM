/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class LineUnderflowException extends SystemException
{
	private String m_line;
	 
	public LineUnderflowException()
	{
		super();
	}

	public LineUnderflowException(String line)
	{
		m_line = line;
	}

	public String getLine()
	{
		return m_line;
	}
}
