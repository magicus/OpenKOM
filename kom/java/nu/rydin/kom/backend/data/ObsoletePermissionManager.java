/*
 * Created on Nov 23, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nu.rydin.kom.structs.ConferencePermission;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ObsoletePermissionManager
{	
	private final Connection m_conn;
	private final PreparedStatement m_addPermission;
	private final PreparedStatement m_getPermissions;
	private final PreparedStatement m_updatePermissions;
	private final PreparedStatement m_revokePermissions;
	private final PreparedStatement m_listUsers;
	
	public ObsoletePermissionManager(Connection conn)
	throws SQLException
	{
		m_conn = conn;
		m_addPermission = conn.prepareStatement(
			"INSERT INTO conferencepermissions(conference, user, permissions, negation_mask) VALUES (?, ?, ?, ?)");
		m_getPermissions = conn.prepareStatement(
			"SELECT p.permissions, p.negation_mask, c.permissions " +			"FROM conferencepermissions p, conferences c LEFT OUTER JOIN conferences ON c.id = m.conference" +			"WHERE p.conference = ? AND p.user = ? AND p.conference = c.id");
		m_updatePermissions = conn.prepareStatement(
			"UPDATE conferencepermissions SET permissions = ?, negation_mask = ? WHERE conference = ? AND user = ?");
		m_revokePermissions = conn.prepareStatement(
			"DELETE FROM conferencepersmissions WHERE conference = ? AND user = ?");
		m_listUsers = conn.prepareStatement(
			"SELECT c.user, n.fullname, c.permissions, c.negation_mask FROM conferencepermissions c, users u " +			"WHERE c.user = n.id AND c.conference = ?");
	}
	
	public void close()
	throws SQLException
	{
		if(m_addPermission != null)
			m_addPermission.close();
		if(m_getPermissions != null)
			m_getPermissions.close();
		if(m_updatePermissions != null)
			m_updatePermissions.close();
		if(m_revokePermissions != null)
			m_revokePermissions.close();
		if(m_listUsers != null)
			m_listUsers.close(); 
	}
	
	
	/**
	 * Returns the permission bitmap for a user in a conference. Returns 0
	 * id the user didn't have any permissions in that conference.
	 * 
	 * @param conf The id of the conference
	 * @param user The id of the user
	 * @throws SQLException
	 */ 
	public int getPermissions(long conf, long user)
	throws SQLException
	{
		m_getPermissions.clearParameters();
		m_getPermissions.setLong(1, conf);
		m_getPermissions.setLong(2, user);
		ResultSet rs = null;
		try
		{
			rs = m_getPermissions.executeQuery();
			if(!rs.next())
				return 0; 
			
			// Get user permission, negation mask and conference permissions
			//
			int u = rs.getInt(1);
			int m = rs.getInt(2);
			int c = rs.getInt(3);
			
			// A permission should be granted if the conference permission
			// or set user permission is granted, unless the negation bit is 
			// set.
			//
			return ((u | c) & ~m); 			
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Returns true if a user has the specified set of permissions.
	 * 
	 * @param conf The id of the conference
	 * @param user The id of the user
	 * @param mask The permission mask. Logically ANDed with stored permissions
	 * 
	 * @throws SQLException
	 */
	public boolean hasPermission(long conf, long user, int mask)
	throws SQLException
	{
		return (this.getPermissions(conf, user) & mask) == mask;
	}
	
	/**
	 * Updates permissions for a user in a conference
	 * 
	 * @param conf The id of the conference
	 * @param user The id of the user
	 * @param permissions The new permission bitmap
	 * @param negations The new negation mask
	 * 
	 * @throws SQLException
	 */
	public void updatePermissions(long conf, long user, int permissions, int negations)
	throws SQLException
	{
		m_updatePermissions.clearParameters();
		m_updatePermissions.setInt(1, permissions);
		m_updatePermissions.setInt(2, negations);
		m_updatePermissions.setLong(3, conf);
		m_updatePermissions.setLong(4, user);
		if(m_updatePermissions.executeUpdate() == 0)
		{
			// Not found! We have to create a new permission record then!
			//
			// this.addConferencePermission(conf, user, permissions, negations);
		}
	}
	
	/**
	 * Revokes all permissions of a user in a conference. Fails silently 
	 * if user did not have any permissions in conference.
	 * 
	 * @param conf The id of the conference
	 * @param user The id of the user
	 * @throws SQLException
	 */
	public void revokeAllPermissions(long conf, long user)
	throws SQLException
	{
		m_revokePermissions.setLong(1, conf);
		m_revokePermissions.setLong(2, user);
		m_revokePermissions.executeUpdate();
	}
	
	/**
	 * Returns a list of user permissions for a conference
	 * 
	 * @param conf The id of the conference
	 * @throws SQLException
	 */
	public ConferencePermission[] listPermissions(long conf)
	throws SQLException
	{
		m_listUsers.clearParameters();
		m_listUsers.setLong(1, conf);
		List list = new ArrayList();
		ResultSet rs = null;
		try
		{
			rs = m_listUsers.executeQuery();
			while(rs.next())
			{
				list.add(new ConferencePermission(
					new NameAssociation(
						rs.getLong(1), 		// Conf id
						rs.getString(2)), 	// Conf name
						rs.getInt(3), 		// Permissions
						rs.getInt(4)		// Negations
						));
			}
			ConferencePermission[] answer = new ConferencePermission[list.size()];
			list.toArray(answer);
			return answer;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
}

