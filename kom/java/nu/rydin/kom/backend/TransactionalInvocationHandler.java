/*
 * Created on Nov 4, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nu.rydin.kom.exceptions.InternalException;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TransactionalInvocationHandler implements InvocationHandler 
{
	private final ServerSessionImpl m_session;
	
	private final CacheManager m_cacheManager = CacheManager.instance();
		
	public TransactionalInvocationHandler(ServerSessionImpl session)
	{
		m_session = session;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable 
	{
		// Don't even try if the session is invalid
		//
		if(!m_session.isValid())
			throw new InternalException("Invalid session!");
		DataAccess da = DataAccessPool.instance().getDataAccess();
		boolean committed = false;
		try
		{
            synchronized(m_session.m_userContext)
            {
    		    // Make sure we're the only ones fiddling with this session
    		    //
    		    m_session.acquireMutex();
    		    m_session.setDataAccess(da);
    		    
    		    // Invoke the method
    		    //
    			Object result = method.invoke(m_session, args);
    			m_session.setDataAccess(null);
    			
    			// TODO: Synch the two commits
    			//
    			da.commit();
    			m_cacheManager.commit();
    			m_session.flushEvents();
    			committed = true;
    			return result;
            }
		}
		catch(InvocationTargetException e)
		{
			// Unwrap InvocationTargetExceptions
			//
			throw e.getTargetException();
		}
		finally
		{
			if(!committed)
			{
				da.rollback();
				m_cacheManager.rollback();
				m_session.discardEvents();
			}
			DataAccessPool.instance().returnDataAccess(da);
			m_session.releaseMutex();
		} 
	}
}
