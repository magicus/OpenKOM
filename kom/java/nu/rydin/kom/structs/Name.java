/*
 * Created on Sep 4, 2004
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
public class Name implements Serializable
{
    public String m_name;
    
    public short m_visibility;
    
    public Name(String name, short visibility)
    {
        m_name 			= name;
        m_visibility 	= visibility;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public short getVisibility()
    {
        return m_visibility;
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
