/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.HashMap;
import java.util.LinkedList;

import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.SessionShutdownEvent;
import nu.rydin.kom.exceptions.UnexpectedException;

/**
 * Holds the currently active sessions.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SessionManager
{
	/**
	 * Currently active sessions keyed by user id
	 */
	private HashMap m_sessions = new HashMap();
	
	/**
	 * Currently active sessions as an ordered list.
	 */
	private LinkedList m_orderedList = new LinkedList();
	
	/**
	 * Queue of events to be broadcasted
	 */
	private LinkedList m_broadcastQueue = new LinkedList();
	
	private class Broadcaster extends Thread
	{
		public Broadcaster() 
		{
			super("Broadcaster");
		}
		
		public void run()
		{
			try
			{
				for(;;)
				{
					SessionManager sm = SessionManager.this;
					ServerSession[] sessions = null;
					Event e = null; 
					synchronized(sm)
					{
						while(sm.m_broadcastQueue.isEmpty())
							sm.wait();
						e = (Event) sm.m_broadcastQueue.removeFirst();
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
					int top = sessions.length;
					for(int idx = 0; idx < top; ++idx)
					{
						ServerSession each = sessions[idx];
						
						// Don't send to originator unless the event explicitly ask
						// for it.
						//
						if(!e.sendToSelf() && each.getLoggedInUserId() == e.getOriginatingUser())
							continue;
						ServerSessionImpl sess = (ServerSessionImpl) sessions[idx];
						sess.acquireMutex();
						try
						{
						    e.dispatch((EventTarget) sessions[idx]);
						}
						finally
						{
						    sess.releaseMutex();
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
	
	private Broadcaster m_broadcaster = new Broadcaster();
	
	private boolean m_allowLogin;
	
	public SessionManager()
	{
		m_broadcaster.start();
		m_allowLogin = true;
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
	
	public void killSession(long sessionId)
	throws UnexpectedException, InterruptedException
	{
		ServerSession session = this.getSession(sessionId);
		
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
			if(this.getSession(sessionId) == null)
				return;
			Thread.sleep(delay);
		}
		
		// Bummer! The session did not shut down when we asked
		// it nicely. Mark it as invalid so that the next request
		// to the server is guaranteed to fail.
		//
		ServerSessionImpl ssi = (ServerSessionImpl) this.getSession(sessionId);
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
		m_sessions.put(new Long(session.getLoggedInUserId()), session);
		m_orderedList.addLast(session);
	}
	
	/**
	 * Unregisteres a session
	 * @param session The session
	 */
	public synchronized void unRegisterSession(ServerSession session)
	{
		m_sessions.remove(new Long(session.getLoggedInUserId()));
		m_orderedList.remove(session);
	}

	/**
	 * Returns a session based on its user id
	 * @param userId The user id
	 */
	public synchronized ServerSession getSession(long userId)
	{
		return (ServerSession) m_sessions.get(new Long(userId));
	}
	
	/**
	 * Lists the sessions in the order they were created.
	 */
	public synchronized ServerSession[] listSessions()
	{
		ServerSession[] answer = new ServerSession[m_orderedList.size()];
		m_orderedList.toArray(answer);
		return answer;
	}
	
	/**
	 * Checks if the given user currently has an open session.
	 * @param u The user ID.
	 */
	
	public synchronized boolean hasSession(long u)
	{
		return m_sessions.containsKey(new Long(u));
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
		ServerSession s;
		synchronized(this)
		{
			s = this.getSession(user);
		}
		
		// Fail silently if we couldn't find the session
		//
		if(s != null)
			e.dispatch((EventTarget) s);
	}
}
