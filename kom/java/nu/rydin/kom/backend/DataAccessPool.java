/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL license.
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
	private static DataAccessPool s_instance;
	
	static
	{
	    try
	    {
	        s_instance = new DataAccessPool();
	    }
	    catch(UnexpectedException e)
	    {
	        throw new ExceptionInInitializerError(e);
	    }
	}
	
	private LinkedList m_pool = new LinkedList(); 
	
	public DataAccessPool()
	throws UnexpectedException
	{
	    int top = ServerSettings.getNumDataAccess();
	    for(int idx = 0; idx < top; ++idx)
	        m_pool.add(this.createDataAccess());
	}
	
	public static DataAccessPool instance()
	{
		return s_instance;
	}
	
	public DataAccess getDataAccess()
	throws UnexpectedException
	{
	    // Have we already requested a da for this thread? 
	    //
		DataAccess da = null;
		synchronized(this)
		{
			if(!m_pool.isEmpty())
				da = (DataAccess) m_pool.removeFirst();
		}
		
		// Did we get anything? Is it working?
		//
		if(da == null || !da.isValid())
			da = this.createDataAccess();
		
		// Associate with thread
		//
		return da;
	}
		
	private DataAccess createDataAccess()
	throws UnexpectedException
	{
		Connection conn = null;
		try
		{
			Class.forName(ServerSettings.getJDBCDriverClass()).newInstance();
			conn = DriverManager.getConnection(ServerSettings.getJDBCConnectString());
			conn.setAutoCommit(false);
//			Statement stmt = conn.createStatement();
//			stmt.execute("SET AUTOCOMMIT=0");
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
	    // Whatever we do, let's not leave uncomitted transactions around
	    //
	    try
	    { 
	        da.rollback();
	    }
	    catch(UnexpectedException e)
	    {
	        // This DataAccess seems broken. Don't return to pool.
	        //
	        return;
	    }
	    
	    // Return to pool
	    //
		m_pool.addLast(da);
	}
}
