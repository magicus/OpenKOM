/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nu.rydin.kom.AmbiguousNameException;
import nu.rydin.kom.DuplicateNameException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.SQLUtils;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NameManager
{
	public static final short PUBLIC = 0;
	public static final short PROTCTED = 1;
	public static final short INVISIBLE = 2;
	
	protected final Connection m_conn;
	private final PreparedStatement m_getNameByIdStmt;
	private final PreparedStatement m_getIdByNameStmt;
	private final PreparedStatement m_getIdsByPatternStmt;
	private final PreparedStatement m_getNamesByPatternStmt;
	private final PreparedStatement m_getIdsByPatternAndKindStmt;
	private final PreparedStatement m_getNamesByPatternAndKindStmt;	
	private final PreparedStatement m_addNameStmt;
	private final PreparedStatement m_getAssociationsByPatternStmt;
	private final PreparedStatement m_getAssociationsByPatternAndKindStmt;
	
	public NameManager(Connection conn)
	throws SQLException
	{
		m_conn = conn;
		m_getNameByIdStmt = conn.prepareStatement(
			"SELECT fullname FROM names WHERE id = ?");		
		m_getIdByNameStmt = conn.prepareStatement(
			"SELECT id FROM names WHERE norm_name = ? AND visibility != 2");		
			
		// TODO: Handle protected names!
		//
		m_getIdsByPatternStmt = conn.prepareStatement(
			"SELECT id FROM names WHERE norm_name LIKE ? AND visibility != 2");
		m_getNamesByPatternStmt = conn.prepareStatement(
			"SELECT fullname FROM names WHERE norm_name LIKE ? AND visibility != 2 " +
		m_getIdsByPatternAndKindStmt = conn.prepareStatement(
			"SELECT id FROM names WHERE norm_name LIKE ? AND kind = ? AND visibility != 2");
		m_getNamesByPatternAndKindStmt = conn.prepareStatement(
			"SELECT fullname FROM names WHERE norm_name LIKE ? AND kind = ? " +
		m_addNameStmt  = conn.prepareStatement(
			"INSERT INTO names(norm_name, fullname, kind, visibility) VALUES(?, ?, ?, ?)");
		m_getAssociationsByPatternStmt = conn.prepareStatement(
			"SELECT id, fullname FROM names WHERE norm_name LIKE ? AND visibility != 2 " +
			"ORDER BY fullname");			
		m_getAssociationsByPatternAndKindStmt = conn.prepareStatement(
			"SELECT id, fullname FROM names WHERE norm_name LIKE ? AND visibility != 2 " +
			"AND kind = ? ORDER BY fullname");
	}
	
	public void finalize()
	{
		try
		{
			this.close();
		}
		catch(SQLException e)
		{
			// Not much we can do here...
			// 
			System.err.println("Warning: Exception in finalizer: " + e.toString());
		}
	}
	
	/**
	 * Returns all resources held by this object
	 */
	public void close()
	throws SQLException
	{
		if(m_getNameByIdStmt != null)
			m_getNameByIdStmt.close();
		if(m_getIdsByPatternStmt != null)
			m_getIdsByPatternStmt.close();
		if(m_getIdByNameStmt != null)
			m_getIdByNameStmt.close();			
		if(m_getNamesByPatternStmt != null)
			m_getNamesByPatternStmt.close();
		if(m_addNameStmt != null)
			m_addNameStmt.close();
		if(m_getAssociationsByPatternStmt != null)
			m_getAssociationsByPatternStmt.close();
		if(m_getAssociationsByPatternAndKindStmt != null)
			m_getAssociationsByPatternAndKindStmt.close();
	}
	
	/**
	 * Returns the name for and id
	 */
	public String getNameById(long id)
	throws ObjectNotFoundException, SQLException
	{
		m_getNameByIdStmt.clearParameters();
		m_getNameByIdStmt.setLong(1, id);
		ResultSet rs = null;
		try
		{
			rs = m_getNameByIdStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("id=" + id);
			return rs.getString(1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Returns a set of name ids matching a search pattern. Each distinct
	 * word is matched separately, such that "Po Ry" matches "Pontus Rydin".
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public String[] getNamesByPattern(String pattern)
	throws SQLException
	{		
		// Transform expressions from "Po (The Man) Ry" to "PO% RY%".
		//
		pattern = this.createKey(pattern);
		
		// Run query
		//
		m_getNamesByPatternStmt.clearParameters();
		m_getNamesByPatternStmt.setString(1, pattern);
		ResultSet rs = null;
		try
		{
			rs = m_getNamesByPatternStmt.executeQuery();
			return SQLUtils.extractStrings(rs, 1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Returns a set of name associations matching a search pattern. Each distinct
	 * word is matched separately, such that "Po Ry" matches "Pontus Rydin".
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public NameAssociation[] getAssociationsByPattern(String pattern)
	throws SQLException
	{		
		// Transform expressions from "Po (The Man) Ry" to "PO% RY%".
		//
		pattern = this.createKey(pattern);
		
		// Run query
		//
		m_getAssociationsByPatternStmt.clearParameters();
		m_getAssociationsByPatternStmt.setString(1, pattern);
		ResultSet rs = null;
		try
		{
			List list = new ArrayList(100);
			rs = m_getAssociationsByPatternStmt.executeQuery();
			while(rs.next())
				list.add(new NameAssociation(rs.getLong(1), rs.getString(2)));
			NameAssociation[] answer = new NameAssociation[list.size()];
			list.toArray(answer);
			return answer;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Returns a set of name associations matching a search pattern and a kind. Each distinct
	 * word is matched separately, such that "Po Ry" matches "Pontus Rydin".
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public NameAssociation[] getAssociationsByPatternAndKind(String pattern, short kind)
	throws SQLException
	{		
		// Transform expressions from "Po (The Man) Ry" to "PO% RY%".
		//
		pattern = this.createKey(pattern);
		
		// Run query
		//
		m_getAssociationsByPatternAndKindStmt.clearParameters();
		m_getAssociationsByPatternAndKindStmt.setString(1, pattern);
		m_getAssociationsByPatternAndKindStmt.setShort(2, kind);
		ResultSet rs = null;
		try
		{
			List list = new ArrayList(100);
			rs = m_getAssociationsByPatternAndKindStmt.executeQuery();
			while(rs.next())
				list.add(new NameAssociation(rs.getLong(1), rs.getString(2)));
			NameAssociation[] answer = new NameAssociation[list.size()];
			list.toArray(answer);
			return answer;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	
	/**
	 * Returns a set of name ids matching a search pattern and a kind. Each distinct
	 * word is matched separately, such that "Po Ry" matches "Pontus Rydin".
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public String[] getNamesByPatternAndKind(String pattern, short kind)
	throws SQLException
	{		
		// Transform expressions from "Po (The Man) Ry" to "PO% RY%".
		//
		pattern = NameUtils.normalizeName(pattern).replaceAll(" ", "% ");
		
		// Run query
		//
		m_getNamesByPatternAndKindStmt.clearParameters();
		m_getNamesByPatternAndKindStmt.setString(1, pattern);
		m_getNamesByPatternAndKindStmt.setShort(2, kind);
		ResultSet rs = null;
		try
		{
			rs = m_getNamesByPatternAndKindStmt.executeQuery();
			return SQLUtils.extractStrings(rs, 1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	
	/**
	 * Returns a set of full names matching a search pattern. Each distinct
	 * word is matched separately, such that "Po Ry" matches "Pontus Rydin".
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public long[] getIdsByPattern(String pattern)
	throws SQLException
	{		
		// Transform expressions from "Po Ry" to "PO% RY%".
		//
		pattern = this.createKey(pattern);
	
		// Run query
		//
		m_getIdsByPatternStmt.clearParameters();
		m_getIdsByPatternStmt.setString(1, pattern);
		ResultSet rs = null;
		try
		{
			rs = m_getIdsByPatternStmt.executeQuery();
			return SQLUtils.extractLongs(rs, 1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Returns a set of full names matching a search pattern and a kind. Each distinct
	 * word is matched separately, such that "Po Ry" matches "Pontus Rydin".
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public long[] getIdsByPatternAndKind(String pattern, short kind)
	throws SQLException
	{		
		// Transform expressions from "Po Ry" to "PO% RY%".
		//
		pattern = this.createKey(pattern);
	
		// Run query
		//
		m_getIdsByPatternAndKindStmt.clearParameters();
		m_getIdsByPatternAndKindStmt.setString(1, pattern);
		m_getIdsByPatternAndKindStmt.setShort(2, kind);
		ResultSet rs = null;
		try
		{
			rs = m_getIdsByPatternAndKindStmt.executeQuery();
			return SQLUtils.extractLongs(rs, 1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	
	/**
	 * Returns the id of an object exactly matching a name
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public long getIdByName(String pattern)
	throws SQLException, ObjectNotFoundException, AmbiguousNameException
	{		
		// Run query
		//
		m_getIdByNameStmt.clearParameters();
		m_getIdByNameStmt.setString(1, NameUtils.normalizeName(pattern));
		ResultSet rs = null;
		try
		{
			rs = m_getIdByNameStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException(pattern);
			long id = rs.getLong(1);
			if(rs.next())
				throw new AmbiguousNameException(pattern);
			return id;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}	
	
	/**
	 * Returns <tt>true</tt> if an exact match for a name exists
	 * @param name The name to test
	 * @throws SQLExcption
	 */
	public boolean nameExists(String name)
	throws SQLException 
	{
		try
		{
			long id = this.getIdByName(name);
			return true;
		}
		catch(ObjectNotFoundException e)
		{
			return false;
		}
		catch(AmbiguousNameException e)
		{
			// It exists, but it's ambiguous!
			// TODO: Is this what we want?
			//
			return true;
		}
	}
	
	public long addName(String name, short kind, short visibility)
	throws SQLException, DuplicateNameException, AmbiguousNameException
	{
		return this.addName(NameUtils.normalizeName(name), name, kind, visibility); 
	}
	
	public long addName(String normName, String fullName, short kind, short visibility)
	throws SQLException, DuplicateNameException, AmbiguousNameException
	{
		// Check to see if we already have the name
		//
		if(this.nameExists(normName))
			throw new DuplicateNameException(normName);
			
		// No name there! Go ahead and add it
		//
		m_addNameStmt.clearParameters();
		m_addNameStmt.setString(1, normName);
		m_addNameStmt.setString(2, fullName);
		m_addNameStmt.setShort(3, kind);
		m_addNameStmt.setShort(4, visibility);
		m_addNameStmt.executeUpdate();
		
		// Now, read it back to obtain the id
		//
		return ((com.mysql.jdbc.PreparedStatement) m_addNameStmt).getLastInsertID();
	}
	
	
	protected String createKey(String name)
	{
		String s = NameUtils.normalizeName(name).toUpperCase().replaceAll(" ", "% ");
		return s.endsWith("%") ? s : s + "%"; 
	}
}