/*
 * Created on Nov 6, 2003
 *
 * Distributed under the GPL license.
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
	private final Name m_name;

	protected NamedObject(long id, Name name)
	{
		m_id 	= id;
		m_name 	= name;
	}
	
	public long getId()
	{
		return m_id;
	}	
	
	public Name getName()
	{
		return m_name;
	}	
}
