/*
 * Created on Jun 6, 2004.
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nu.rydin.kom.ObjectNotFoundException;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ObjectManager 
{
	private final PreparedStatement m_getKindStmt;
	
	public static final short UNKNOWN_KIND = -1;
	
	public ObjectManager(Connection conn)
	throws SQLException
	{
		m_getKindStmt = conn.prepareStatement(
			"select kind from names where id=?");
	}
	
	public void close()
	{
		try
		{
			if(m_getKindStmt != null)
				m_getKindStmt.close();
		}
		catch(SQLException e)
		{
			// Not much we can do here...
			//
			e.printStackTrace();
		}
	}
	
	public short getObjectKind (long objectId)
	throws ObjectNotFoundException
	{
		ResultSet rs = null;
		try
		{
			m_getKindStmt.clearParameters();
			m_getKindStmt.setLong(1, objectId);
			rs = m_getKindStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("Object ID=" + objectId);
			return rs.getInt(1) == 0 ? UserManager.USER_KIND : ConferenceManager.CONFERENCE_KIND;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return 0;
		}
		finally
		{
			try
			{
				if(rs != null)
					rs.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	// TODO: Write implementation to retrieve multiple object types in one call.
}
