/*
 * Created on Nov 5, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NameAssociation implements Serializable
{
	private long m_id;
	
	private String m_name;
	
	public NameAssociation(long id, String name)
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
	
    public String toString() {
        return getName() + "<" + getId() + ">";
    }
}
