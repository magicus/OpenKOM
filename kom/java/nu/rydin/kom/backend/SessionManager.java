/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.backend;

import java.util.HashMap;
import java.util.LinkedList;

import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;

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
						e.dispatch((EventTarget) sessions[idx]);
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
	
	public SessionManager()
	{
		m_broadcaster.start();
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
