/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.SessionShutdownEvent;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.utils.Logger;

/**
 * Holds the currently active sessions.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SessionManager
{
	/**
	 * Currently active sessions keyed by session id
	 */
	private Map<Integer, ServerSession> m_sessionsById = Collections.synchronizedMap(new HashMap<Integer, ServerSession>());
	
	/**
	 * Currently active sessions as an ordered list.
	 */
	private List<ServerSession> m_orderedList = Collections.synchronizedList(new LinkedList<ServerSession>());
	
	/**
	 * Queue of events to be broadcasted
	 */
	private LinkedList<Event> m_broadcastQueue = new LinkedList<Event>();
	
	private class Broadcaster extends Thread
	{
	    private int INITIAL_RETRY_WAIT 	= 1000;
	    private int RETRY_WAIT_INC	 	= 1000;
	    private int MAX_RETRY_WAIT	 	= 5000;
	    
		public Broadcaster() 
		{
			super("Broadcaster");
			this.setDaemon(true);
		}
		
		public void run()
		{
			try
			{
				for(;;)
				{
					SessionManager sm = SessionManager.this;
					List<ServerSession> sessions = null;
					Event e = null; 
					synchronized(sm)
					{
						while(sm.m_broadcastQueue.isEmpty())
							sm.wait();
						e = sm.m_broadcastQueue.removeFirst();
						sessions = sm.listSessions();
					}
					
					// Note: We're working on a snapshot here, which means that
					// we might actually end up sending events to sessions that 
					// are on their way down and miss sessions that are appearing
					// as we do this. The alternative would have been to lock the 
					// queue while we're doing this. Since the possible race-condition
					// is reasonably benign and the cost of locking the queue might
					// potentially be high (event handlers do not execute in 
					// deterministic time), we accept that trade-off.
					//
					int retryWait = INITIAL_RETRY_WAIT;
                    
                    // Create snapshot to iterate over
                    //
					for(Iterator<ServerSession> itor = sessions.iterator(); itor.hasNext();)
					{
					    for(;;)
					    {
						    // We absolutely don't want this thread to die, so we need
						    // to handle exceptions carefully.
						    //
						    try
						    {
								ServerSession each = itor.next();
								
								// Don't send to originator unless the event explicitly ask
								// for it.
								//
								if(!e.sendToSelf() && each.getLoggedInUserId() == e.getOriginatingUser())
									break;
								ServerSessionImpl sess = (ServerSessionImpl) each;
								sess.acquireMutex();
								try
								{
								    e.dispatch((EventTarget) each);
								}
								finally
								{
								    sess.releaseMutex();
								}
								
								// If we get here, we didn't run into any problems
								// and we don't have to retry.
								//
								break;
						    }
						    catch(InterruptedException ex)
						    {
						        // Shutting down, we're outta here!
						        //
						        throw ex;
						    }
						    catch(Throwable t)
						    {
						        // Log this!
						        //
						        Logger.error(this, "Exception in Broadcaster", t);
						        
						        // Unhandled exception. Wait and retry. We increase the wait 
						        // for every failure and eventually give up.
						        //
						        if(retryWait > MAX_RETRY_WAIT)
						        {
						            // We've exceeded the max retry time. Skip this session!
						            //
						            Logger.warn(this, "Giving up!");
                                    retryWait = INITIAL_RETRY_WAIT;
						            break;
						        }
						        
						        // Try again, but first, wait a little
						        //
						        Logger.warn(this, "Retrying... ");
						        Thread.sleep(retryWait);
						        retryWait += RETRY_WAIT_INC;
						    }
					    } 
					}
				}
			}
			catch(InterruptedException e)
			{
				// Graceful shutdown
			}
		}
	}
	
	private Broadcaster m_broadcaster;
	
	private boolean m_allowLogin;
	
	public SessionManager()
	{
		m_allowLogin = true;
	}
	
	public void start()
	{
	    m_broadcaster = new Broadcaster();
	    m_broadcaster.start();
	}
	
	public void stop()
	{
	    m_broadcaster.interrupt();
	}
	
	public void join()
	throws InterruptedException
	{
	    m_broadcaster.join();
	}
	
	public boolean canLogin()
	{
	    return m_allowLogin;
	}
	
	public void allowLogin()
	{
	    m_allowLogin = true;
	}
	
	public void prohibitLogin()
	{
	    m_allowLogin = false;
	}
	
	public void killSessionById(int sessionId)
	throws UnexpectedException, InterruptedException
	{
		ServerSession session = this.getSessionById(sessionId);
		
		// Not logged in? Nothing to shut down. Fail silently.
		//
		if(session == null)
			return;
			
		// Post shutdown event
		//
		session.postEvent(new SessionShutdownEvent());
		
		// Wait for session to terminate
		//
		int top = ServerSettings.getSessionShutdownRetries();
		long delay = ServerSettings.getSessionShutdownDelay();
		while(top-- > 0)
		{
			// Has it disappeared yet?
			//
			if(this.getSessionById(sessionId) == null)
				return;
			Thread.sleep(delay);
		}
		
		// Bummer! The session did not shut down when we asked
		// it nicely. Mark it as invalid so that the next request
		// to the server is guaranteed to fail.
		//
		ServerSessionImpl ssi = (ServerSessionImpl) this.getSessionById(sessionId);
		this.unRegisterSession(ssi);
		
		// Did it dissapear while we were fiddling around? 
		// Well... That's exactly what we want!
		// Note that it may also disappear while we're marking
		// it as invalid, but since that race-condition is completely
		// harmless, we don't waste time synchronizing.
		//
		if(ssi == null)
			return;
		ssi.markAsInvalid();
	}
		
	/**
	 * Registers a session
	 * @param session The session
	 */
	public synchronized void registerSession(ServerSession session)
	{
		m_sessionsById.put(session.getSessionId(), session);
		m_orderedList.add(session);
	}
	
	/**
	 * Unregisteres a session
	 * @param session The session
	 */
	public synchronized void unRegisterSession(ServerSession session)
	{
		m_sessionsById.remove(session.getSessionId());
		m_orderedList.remove(session);
	}

	/**
	 * Returns a session based on its user id
	 * @param sessionId The session id
	 */
	public synchronized ServerSession getSessionById(int sessionId)
	{
		return (ServerSession) m_sessionsById.get(sessionId);
	}
	
	/**
	 * Lists the sessions in the order they were created.
	 */
	public synchronized List<ServerSession> listSessions()
	{
        return new LinkedList<ServerSession>(m_orderedList);
	}
    
    public List<ServerSession> getSessionsByUser(long u)
    {
        ArrayList<ServerSession> list = new ArrayList<ServerSession>();
        for(Iterator<ServerSession> itor = m_orderedList.iterator(); itor.hasNext();)
        {
            ServerSession each = itor.next();
            if(each.getLoggedInUserId() == u)
                list.add(each);
        }
        return list;
    }
	
	/**
	 * Checks if the given user currently has an open session.
	 * @param u The user ID.
	 */
	public synchronized boolean userHasSession(long u)
	{
        return this.getSessionsByUser(u).size() > 0;
	}
	
	/**
	 * Broadcasts an event to all currently active sessions
	 * @param e The event
	 */
	public synchronized void broadcastEvent(Event e)
	{
		m_broadcastQueue.addLast(e);
		this.notify();
	}
	
	/**
	 * Sends an event to a specified user
	 * @param user The user
	 * @param e The event
	 */
	public void sendEvent(long user, Event e)
	{
		List<ServerSession> s;
		synchronized(this)
		{
			s = this.getSessionsByUser(user);
		}
		
		// Fail silently if we couldn't find any sessions for the user
		//
        for(Iterator<ServerSession> itor = s.iterator(); itor.hasNext();)
        {
			e.dispatch((EventTarget) itor.next());
        }
	}
}
