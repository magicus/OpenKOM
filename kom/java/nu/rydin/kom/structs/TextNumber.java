/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class TextNumber
{
	private boolean m_global;

	private int m_number;

	public TextNumber(int number)
	{
		m_number = number;
		m_global = false;
	}

	public TextNumber(int number, boolean global)
	{
		m_number = number;
		m_global = global;
	}

	public int getNumber()
	{
		return m_number;
	}

	public boolean isGlobal()
	{
		return m_global;
	}
}