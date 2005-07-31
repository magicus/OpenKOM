/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author Magnus Ihse Bursie (magnus@ihse.net)
 */
public class TextNumber
{
	private boolean m_global;

	private long m_number;

	public TextNumber(long number)
	{
		m_number = number;
		m_global = false;
	}

	public TextNumber(long number, boolean global)
	{
		m_number = number;
		m_global = global;
	}

	public long getNumber()
	{
		return m_number;
	}

	public boolean isGlobal()
	{
		return m_global;
	}
}