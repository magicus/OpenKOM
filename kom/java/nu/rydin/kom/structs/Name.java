/*
 * Created on Sep 4, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

import java.io.Serializable;

import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.constants.Visibilities;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Name implements Serializable
{
	static private Name s_emptyName = new Name("", Visibilities.PUBLIC, NameManager.UNKNOWN_KIND);
    private String m_name;
    
    private short m_visibility;
    
    private short m_kind;
    
    public static Name emptyName()
    {
    	return s_emptyName;
    }
    
    public Name(String name, short visibility, short kind)
    {
        m_name 			= name;
        m_visibility 	= visibility;
        m_kind 			= kind;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public short getVisibility()
    {
        return m_visibility;
    }
    
    public short getKind()
    {
    	return m_kind;
    }
    
    public void hideName()
    {
        m_name = "";
        m_visibility = Visibilities.PROTECTED;
    }
    
    public String toString()
    {
        return m_name.length() > 0 ? m_name : "(PROTECTED)"; 
    }
}
