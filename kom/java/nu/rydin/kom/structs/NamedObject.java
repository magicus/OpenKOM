/*
 * Created on Nov 6, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NamedObject implements Serializable
{
	private final long m_id;
	private final String m_name;

	protected NamedObject(long id, String name)
	{
		m_id 	= id;
		m_name 	= name;
	}
	
	public long getId()
	{
		return m_id;
	}	
	
	public String getName()
	{
		return m_name;
	}	
}
