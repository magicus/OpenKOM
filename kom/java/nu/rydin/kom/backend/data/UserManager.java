/*
 * Created on Oct 5, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import nu.rydin.kom.AlreadyMemberException;
import nu.rydin.kom.AmbiguousNameException;
import nu.rydin.kom.AuthenticationException;
import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.DuplicateNameException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.structs.UserInfo;

public class UserManager // extends NameManager 
{	
	private final PreparedStatement m_getIdByLoginStmt;
	private final PreparedStatement m_authenticateStmt;
	private final PreparedStatement m_addUserStmt;
	private final PreparedStatement m_loadUserStmt;
	private final PreparedStatement m_updateCharsetStmt;
	
	private final NameManager m_nameManager;
	
	private final Connection m_conn;
	
	public static final long SYSOP_FLAG = 1L;
	
	public static final short USER_KIND = 0;
	
	public UserManager(Connection conn, NameManager nameManager)
	throws SQLException
	{
		m_conn = conn;
		m_nameManager = nameManager;
		m_getIdByLoginStmt = conn.prepareStatement(
			"SELECT id FROM users WHERE userid = ?");
		m_authenticateStmt = conn.prepareStatement(
			"SELECT u.id, u.pwddigest FROM users u, names n WHERE u.userid = ? AND n.id = u.id");
		m_addUserStmt = conn.prepareStatement(
			"INSERT INTO users(userid, pwddigest, address1, address2, " +
			"address3, address4, phoneno1, phoneno2, email1, email2, url, charset, id, flags, rights) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		m_loadUserStmt = conn.prepareStatement(			"SELECT n.fullname, u.userid, u.address1, u.address2, u.address3, u.address4, " +
			"u.phoneno1, u.phoneno2, u.email1, u.email2, u.url, u.charset, u.flags, u.rights, u.locale " +
			"FROM users u, names n WHERE u.id = ? AND n.id = u.id");
		m_updateCharsetStmt = conn.prepareStatement(
			"UPDATE users SET charset = ? WHERE id = ?");
	}
	
	/**
	 * Return resources used by this object
	 */
	public void close()
	throws SQLException
	{
		if(m_getIdByLoginStmt != null)
			m_authenticateStmt.close();
		if(m_authenticateStmt != null)
			m_authenticateStmt.close();
		if(m_addUserStmt != null)
			m_addUserStmt.close();
		if(m_loadUserStmt != null)
			m_loadUserStmt.close();
		if(m_updateCharsetStmt != null)
			m_updateCharsetStmt.close();																	
	}
	
	/**
	 * Authenticates a user
	 * 
	 * @param userid The user id
	 * @param password The password
	 * @throws ObjectNotFoundException The user didn't exist
	 * @throws NoSuchAlgorithmException
	 * @throws SQLException
	 */
	public long authenticate(String userid, String password)
	throws ObjectNotFoundException, AuthenticationException,
		NoSuchAlgorithmException, SQLException
	{
		// Look up user
		//
		m_authenticateStmt.clearParameters();
		m_authenticateStmt.setString(1, userid);
		ResultSet rs = null;
		try
		{
			rs = m_authenticateStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException(userid);
			long id = rs.getLong(1);
			byte[] candidate = rs.getBytes(2);
				
			// Calculate digest of supplied password
			// 
			byte[] digest = this.passwordDigest(password);
				
			// Compare to digest in database
			//
			if(!Arrays.equals(digest, candidate))
				throw new AuthenticationException(userid);
			return id;
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
		
	/**
	 * Adds a new user
	 * 
	 * @throws DuplicateNameException
	 * @throws SQLException
	 * @throws NoSuchAlgorithmException
	 */
	public void addUser(String userid, String password, String fullname, String address1,
		String address2, String address3, String address4, String phoneno1, 
		String phoneno2, String email1, String email2, String url, String charset, long flags, long rights)
		throws DuplicateNameException, SQLException, NoSuchAlgorithmException,
		AmbiguousNameException
		{
			if(this.userExists(userid)) 
				throw new DuplicateNameException(userid);
			if(m_nameManager.nameExists(fullname))
				throw new DuplicateNameException(fullname);
			// First, add the name
			//
			long nameId = m_nameManager.addName(fullname, USER_KIND, NameManager.PUBLIC);
			
			// Now, add the user
			//
			m_addUserStmt.clearParameters();
			m_addUserStmt.setString(1, userid);
			m_addUserStmt.setBytes(2, this.passwordDigest(password));
			m_addUserStmt.setString(3, address1);
			m_addUserStmt.setString(4, address2);
			m_addUserStmt.setString(5, address3);
			m_addUserStmt.setString(6, address4);
			m_addUserStmt.setString(7, phoneno1);
			m_addUserStmt.setString(8, phoneno2);
			m_addUserStmt.setString(9, email1);
			m_addUserStmt.setString(10, email2);
			m_addUserStmt.setString(11, url);
			m_addUserStmt.setString(12, charset);
			m_addUserStmt.setLong(13, nameId);
			m_addUserStmt.setLong(14, flags);
			m_addUserStmt.setLong(15, rights);
			
			// Lock cache while updating
			//
			m_addUserStmt.executeUpdate();
			
			// Add a mailbox
			//
			new ConferenceManager(m_conn, m_nameManager).addMailbox(nameId, fullname, 0);
			
			// Make us a member of our mailbox
			//
			try
			{
				new MembershipManager(m_conn).signup(nameId, nameId, 0, ConferencePermissions.ALL_PERMISSIONS, 0);
			}
			catch(AuthorizationException e)
			{
				// This can't happen!
				// 
				throw new RuntimeException("This can't happen!", e);
			}
			catch(AlreadyMemberException e)
			{
				// This can't happen!
				// 
				throw new RuntimeException("This can't happen!", e);
			}
			catch(ObjectNotFoundException e)
			{
				// This can't happen!
				// 
				throw new RuntimeException("This can't happen!", e);
			}
			
		}
		
	public long getUserIdByLogin(String login)
	throws ObjectNotFoundException, SQLException
	{
		m_getIdByLoginStmt.clearParameters();
		m_getIdByLoginStmt.setString(1, login);
		ResultSet rs = null;
		try
		{
			rs = m_getIdByLoginStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException(login);
			return rs.getLong(1);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}	
	
	public boolean userExists(String login)
	throws SQLException
	{
		try
		{
			this.getUserIdByLogin(login);
			return true;
		}
		catch(ObjectNotFoundException e)
		{
			return false;
		}
	}
	
	/**
	 * Returns a list of user names based on a search pattern
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public String[] getUserNamesByPattern(String pattern)
	throws SQLException
	{
		return m_nameManager.getNamesByPatternAndKind(pattern, USER_KIND);
	}
	
	/**
	 * Returns a list of user ids based on a search pattern
	 * @param pattern The search pattern
	 * @throws SQLException
	 */
	public long[] getUserIdsByPattern(String pattern)
	throws SQLException
	{
		return m_nameManager.getIdsByPatternAndKind(pattern, USER_KIND);
	}
	
	public UserInfo loadUser(long id)
	throws ObjectNotFoundException, SQLException
	{
		m_loadUserStmt.clearParameters();
		m_loadUserStmt.setLong(1, id);
		ResultSet rs = null;
		try
		{
			rs = m_loadUserStmt.executeQuery();
			if(!rs.next())
				throw new ObjectNotFoundException("id=" + id);
			return new UserInfo(
				id,					// id
				rs.getString(1),	// userid
				rs.getString(2),	// name
				rs.getString(3),	// address1
				rs.getString(4),	// address2
				rs.getString(5),	// address3
				rs.getString(6),	// address4
				rs.getString(7),	// phoneno1 
				rs.getString(8),	// phoneno2
				rs.getString(9),	// email1
				rs.getString(10),	// email2
				rs.getString(11),	// url,
				rs.getString(12),	// charset
				rs.getLong(13),		// flags,
				rs.getLong(14),		// rights
				rs.getString(15)	// locale
			);
		}
		finally
		{
			if(rs != null)
				rs.close();
		}
	}
	
	/**
	 * Updates the character set setting of a user
	 * @param userId
	 * @param charset
	 * @throws ObjectNotFoundException
	 * @throws SQLException
	 */
	public void updateCharacterset(long userId, String charset)
	throws ObjectNotFoundException, SQLException
	{
		m_updateCharsetStmt.clearParameters();
		m_updateCharsetStmt.setString(1, charset);
		m_updateCharsetStmt.setLong(2, userId);
		int n = m_updateCharsetStmt.executeUpdate();
		if(n == 0)
			throw new ObjectNotFoundException();
	}
	
	
	/**
	 * Calculates an MD5 digest of a password
	 * @param password
	 * @return The MD5 digest
	 * @throws NoSuchAlgorithmException
	 */
	protected byte[] passwordDigest(String password)
	throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(password.getBytes());
		return md.digest();		
	}
}
