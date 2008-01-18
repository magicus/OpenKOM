/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nu.rydin.kom.backend.CacheManager;
import nu.rydin.kom.backend.KOMCache;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.SQLUtils;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.EmailRecipientNotRecognizedException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class NameManager
{
    // Different kinds of named objects
    //
	public static final short UNKNOWN_KIND = -1;
    public static final short USER_KIND = 0;
    public static final short CONFERENCE_KIND = 1;
	
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
	private final PreparedStatement m_renameObjectStmt;
	private final PreparedStatement m_getKindStmt;
	private final PreparedStatement m_dropNamedObjectStmt;
	private final PreparedStatement m_changeVisibilityStmt;
    private final PreparedStatement m_searchObjectStmt;
    private final PreparedStatement m_changeKeywordsStmt;
    private final PreparedStatement m_changeEmailAliasStmt;
    private final PreparedStatement m_findByEmailAlias;
	
	public NameManager(Connection conn)
	throws SQLException
	{
		m_conn = conn;
		m_getNameByIdStmt = conn.prepareStatement(
			"SELECT fullname, visibility, kind FROM names WHERE id = ?");		
		m_getIdByNameStmt = conn.prepareStatement(
			"SELECT id FROM names WHERE norm_name = ?");		
		m_getIdsByPatternStmt = conn.prepareStatement(
			"SELECT id FROM names WHERE norm_name LIKE ?");
		m_getNamesByPatternStmt = conn.prepareStatement(
			"SELECT id, fullname, kind FROM names WHERE norm_name LIKE ? " +			"ORDER BY fullname");
		m_getIdsByPatternAndKindStmt = conn.prepareStatement(
			"SELECT id FROM names WHERE norm_name LIKE ? AND kind = ?");
		m_getNamesByPatternAndKindStmt = conn.prepareStatement(
			"SELECT fullname, visibility, kind FROM names WHERE norm_name LIKE ? AND kind = ? " +			"ORDER BY fullname");
		m_addNameStmt  = conn.prepareStatement(
			"INSERT INTO names(norm_name, fullname, kind, visibility, keywords) VALUES(?, ?, ?, ?, ?)");
		m_getAssociationsByPatternStmt = conn.prepareStatement(
			"SELECT id, fullname, visibility, kind FROM names WHERE norm_name LIKE ? " +
			"ORDER BY fullname");			
		m_getAssociationsByPatternAndKindStmt = conn.prepareStatement(
			"SELECT id, fullname, visibility, kind FROM names WHERE norm_name LIKE ? " +
			"AND kind = ? ORDER BY fullname");
		m_renameObjectStmt = conn.prepareStatement(
			"UPDATE names SET fullname = ?, norm_name = ? WHERE id = ?");
		m_getKindStmt = conn.prepareStatement(
			"select kind from names where id=?");
		m_dropNamedObjectStmt = conn.prepareStatement(
			"delete from names where id = ?");
		m_changeVisibilityStmt = conn.prepareStatement(
		    "UPDATE names SET visibility = ? WHERE id = ?");
        m_searchObjectStmt = conn.prepareStatement(
            "SELECT id, fullname, visibility, kind FROM names WHERE fullname like ? OR keywords LIKE ?");
        m_changeKeywordsStmt = conn.prepareStatement(
            "UPDATE names SET keywords = ? WHERE id = ?");
        m_changeEmailAliasStmt = conn.prepareStatement(
            "UPDATE names SET emailalias = ? WHERE id = ?");    
        m_findByEmailAlias = conn.prepareStatement(
            "SELECT id FROM names WHERE emailalias = ?");
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
		if(m_renameObjectStmt != null)
			m_renameObjectStmt.close();
		if (m_getKindStmt != null)
			m_getKindStmt.close();
		if (m_dropNamedObjectStmt != null)
			m_dropNamedObjectStmt.close();
		if(m_changeVisibilityStmt != null)
		    m_changeVisibilityStmt.close();
        if(m_searchObjectStmt != null)
            m_searchObjectStmt.close();
        if(m_changeKeywordsStmt != null)
            m_changeKeywordsStmt.close();
        if(m_changeEmailAliasStmt != null)
            m_changeEmailAliasStmt.close();
        if(m_findByEmailAlias != null)
            m_findByEmailAlias.close();                                
	}
	
	/**
	 * Returns the name for and id
	 */
	public Name getNameById(long id)
	throws ObjectNotFoundException, SQLException
	{
	    // Check cache first!
	    //
	    KOMCache cache = CacheManager.instance().getNameCache();
	    Name name = (Name) cache.get(new Long(id));
	    if(name != null)
	        return name;
		m_getNameByIdStmt.clearParameters();
		m_getNameByIdStmt.setLong(1, id);
		ResultSet rs = null;
		try
		{
			rs = m_getNameByIdStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("id=" + id);
			Name answer = new Name(rs.getString(1), rs.getShort(2), rs.getShort(3));
			cache.deferredPut(new Long(id), answer);
			return answer;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	public void changeVisibility(long id, short visibility)
	throws SQLException
	{
	    // Update database
	    //
	    m_changeVisibilityStmt.clearParameters();
	    m_changeVisibilityStmt.setShort(1, visibility);
	    m_changeVisibilityStmt.setLong(2, id);
	    m_changeVisibilityStmt.executeUpdate();
	    
	    // Invalidate caches
	    //	    
	    this.invalidateCache(id);
	}
	
    /**
	 * Returns a set of name ids matching a search pattern. Each distinct
	 * word is matched separately, such that "Po Ry" matches "Pontus Rydin".
	 * 
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public Name[] getNamesByPattern(String pattern)
	throws SQLException
	{		
		// Run query
		//
		m_getNamesByPatternStmt.clearParameters();
		m_getNamesByPatternStmt.setString(1, this.createKey(pattern));
		ResultSet rs = null;
		try
		{
			rs = m_getNamesByPatternStmt.executeQuery();
			return SQLUtils.extractStrings(rs, 1, 2, 3, pattern);
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
		// Run query
		//
		m_getAssociationsByPatternStmt.clearParameters();
		m_getAssociationsByPatternStmt.setString(1, this.createKey(pattern));
		ResultSet rs = null;
		try
		{
			rs = m_getAssociationsByPatternStmt.executeQuery();
			return SQLUtils.extractNames(rs, 1, 2, 3, 4, pattern);
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
		// Run query
		//
		m_getAssociationsByPatternAndKindStmt.clearParameters();
		m_getAssociationsByPatternAndKindStmt.setString(1, this.createKey(pattern));
		m_getAssociationsByPatternAndKindStmt.setShort(2, kind);
		ResultSet rs = null;
		try
		{
			rs = m_getAssociationsByPatternAndKindStmt.executeQuery();
			return SQLUtils.extractNames(rs, 1, 2, 3, 4, pattern);
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
	public Name[] getNamesByPatternAndKind(String pattern, short kind)
	throws SQLException
	{		
	    // Run query
		//
		m_getNamesByPatternAndKindStmt.clearParameters();
		m_getNamesByPatternAndKindStmt.setString(1, 
		        NameUtils.normalizeName(pattern).replaceAll(" ", "% "));
		m_getNamesByPatternAndKindStmt.setShort(2, kind);
		ResultSet rs = null;
		try
		{
			rs = m_getNamesByPatternAndKindStmt.executeQuery();
			return SQLUtils.extractStrings(rs, 1, 2, 3, pattern);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
    
    /**
     * Searches objects based on partial match of either name or keyword
     * 
     * @param pattern The pattern to search for
     * @return An array of matching <tt>NameAssociations</tt>
     * @throws SQLException
     */
    public NameAssociation[] findObjects(String pattern)
    throws SQLException
    {
        pattern = '%' + pattern + '%';
        m_searchObjectStmt.clearParameters();
        m_searchObjectStmt.setString(1, pattern);
        m_searchObjectStmt.setString(2, pattern);
        ResultSet rs = null;
        try
        {
            rs = m_searchObjectStmt.executeQuery();
            return SQLUtils.extractNames(rs, 1, 2, 3, 4, null);
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
	 * @throws SQLException
	 */
	public boolean nameExists(String name)
	throws SQLException 
	{
		try
		{
			this.getIdByName(name);
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
	
	public long addName(String name, short kind, short visibility, String keywords)
	throws SQLException, DuplicateNameException, AmbiguousNameException
	{
		return this.addName(NameUtils.normalizeName(name), name, kind, visibility, keywords); 
	}
	
	public long addName(String normName, String fullName, short kind, short visibility, String keywords)
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
        m_addNameStmt.setString(5, keywords);
		m_addNameStmt.executeUpdate();
		
		// Now, read it back to obtain the id
		//
		return ((com.mysql.jdbc.PreparedStatement) m_addNameStmt).getLastInsertID();
	}
	
	/**
	 * Renames an object
	 * @param id The id of the Object to rename
	 * @param newName The new name
	 * @throws SQLException
	 * @throws ObjectNotFoundException
	 * @throws DuplicateNameException
	 */
	public void renameObject(long id, String newName)
	throws SQLException, ObjectNotFoundException, DuplicateNameException
	{
		String normName = NameUtils.normalizeName(newName);

		// Check to see if we already have the name
		//
		if(this.nameExists(normName))
			throw new DuplicateNameException(normName);
		
		// Update name
		//
		m_renameObjectStmt.clearParameters();
		m_renameObjectStmt.setString(1, newName);
		m_renameObjectStmt.setString(2, normName);
		m_renameObjectStmt.setLong(3, id);
		if(m_renameObjectStmt.executeUpdate() == 0)
			throw new ObjectNotFoundException("id=" + id);
		
		// Update caches
		//
		invalidateCache(id);
	}
	
    /**
     * Changes object keywords
     * 
     * @param id The id of the object to change
     * @param keywords The new set of keywords (comma separated)
     * @throws SQLException
     */
    public void changeKeywords(long id, String keywords)
    throws SQLException
    {
        m_changeKeywordsStmt.clearParameters();
        m_changeKeywordsStmt.setString(1, keywords);
        m_changeKeywordsStmt.setLong(2, id);
        m_changeKeywordsStmt.executeUpdate();
        
        // Invalidate caches
        //      
        this.invalidateCache(id);
    }

    /**
     * Changes object email alias
     * 
     * @param id The id of the object to change
     * @param emailAlias The new email alias
     * @throws SQLException
     */
    public void changeEmailAlias(long id, String emailAlias)
    throws SQLException
    {
        m_changeEmailAliasStmt.clearParameters();
        m_changeEmailAliasStmt.setString(1, emailAlias);
        m_changeEmailAliasStmt.setLong(2, id);
        m_changeEmailAliasStmt.executeUpdate();
        
        // Invalidate caches
        //      
        this.invalidateCache(id);
    }
    
    public long findByEmailAlias(String alias)
    throws SQLException, EmailRecipientNotRecognizedException
    {
        m_findByEmailAlias.clearParameters();
        m_findByEmailAlias.setString(1, alias);
        ResultSet rs = null;
        try
        {
        	rs = m_findByEmailAlias.executeQuery();
        	if(!rs.next())
        		throw new EmailRecipientNotRecognizedException(alias);
        	return rs.getLong(1);
        }
        finally
        {
        	if(rs != null)
        		rs.close();
        }
    }
    
	protected String createKey(String name)
	{
		String s = NameUtils.normalizeName(name).toUpperCase().replaceAll(" ", "% ");
		return s.endsWith("%") ? s : s + "%"; 
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
			return rs.getInt(1) == 0 ? NameManager.USER_KIND : NameManager.CONFERENCE_KIND;
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
	
	public void dropNamedObject (long objectId)
	throws SQLException
	{
		this.m_dropNamedObjectStmt.clearParameters();
		this.m_dropNamedObjectStmt.setLong(1, objectId);
		this.m_dropNamedObjectStmt.execute();
		
		// Update caches
		//
		invalidateCache(objectId);
	}
	
    private void invalidateCache(long id)
    {
        Long key = new Long(id);
		CacheManager cmgr = CacheManager.instance();
		cmgr.getUserCache().registerInvalidation(key);
		cmgr.getConferenceCache().registerInvalidation(key);
		cmgr.getNameCache().registerInvalidation(key);
    }
}