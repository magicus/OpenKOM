/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import nu.rydin.kom.exceptions.UnexpectedException;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class DataAccessPool 
{
	private static DataAccessPool s_instance = new DataAccessPool();
	
	private LinkedList m_pool = new LinkedList(); 
	
	public static DataAccessPool instance()
	{
		return s_instance;
	}
	
	public DataAccess getDataAccess()
	throws UnexpectedException
	{
		DataAccess da = null;
		synchronized(this)
		{
			if(!m_pool.isEmpty())
				da = (DataAccess) m_pool.removeFirst();
		}
		
		// Did we get anything? Is it working?
		//
		if(da != null && da.isValid())
			return da;
		
		Connection conn = null;
		try
		{
			Class.forName(ServerSettings.getJDBCDriverClass()).newInstance();
			conn = DriverManager.getConnection(ServerSettings.getJDBCConnectString());
			conn.setAutoCommit(false);
			return new DataAccess(conn);			
		}
		catch(IllegalAccessException e)
		{
			throw new UnexpectedException(-1, "PANIC: Can't access driver", e);
		}
		catch(ClassNotFoundException e)
		{
			throw new UnexpectedException(-1, "PANIC: Can't find driver", e);
		}
		catch(InstantiationException e)
		{
			throw new UnexpectedException(-1, "PANIC: Can't instantiate driver", e); 
		}
		catch(SQLException e)
		{
			throw new UnexpectedException(-1, "PANIC: Error while creating connection", e);
		}		
	}
	
	public synchronized void returnDataAccess(DataAccess da)
	{
		m_pool.addLast(da);
	}
}
