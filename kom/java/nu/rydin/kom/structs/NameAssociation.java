/*
 * Created on Nov 5, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

import nu.rydin.kom.constants.Visibilities;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NameAssociation implements Serializable
{
	private long m_id;
	
	private Name m_name;
	
	public NameAssociation(long id, Name name)
	{
		m_id 	= id;
		m_name 	= name;
	}
	
	public NameAssociation(long id, String name)
	{
		m_id 	= id;
		m_name 	= new Name(name, Visibilities.PUBLIC);
	}
	
	public long getId()
	{
		return m_id;
	}

	public Name getName()
	{
		return m_name;
	}
	
    public String toString() {
        return getName() + "<" + getId() + ">";
    }
}
