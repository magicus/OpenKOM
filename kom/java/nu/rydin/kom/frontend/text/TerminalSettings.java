/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TerminalSettings
{
	private int m_height;
	
	private int m_width;
	
	private String m_type;
	
	public TerminalSettings(int height, int width, String type)
	{
		m_height 	= height;
		m_width 	= width;
		m_type 		= type;
	}

	public int getHeight()
	{
		return m_height;
	}

	public String getType()
	{
		return m_type;
	}

	public int getWidth()
	{
		return m_width;
	}
}
