/*
 * Created on Nov 25, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ConferencePermission
{
	private NameAssociation m_user;
	
	private int m_permissions;
	
	private int m_negations;
	
	public ConferencePermission(NameAssociation user, int permissions, int negations)
	{
		m_user 			= user;
		m_permissions 	= permissions;
		m_negations		= negations;
	}
	
	public NameAssociation getUser()
	{
		return m_user;
	}
	
	public int getPermissions()
	{
		return m_permissions;
	}
	
	public int getNegations()
	{
		return m_negations;
	}
}
