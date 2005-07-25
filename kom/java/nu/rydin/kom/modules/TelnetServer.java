/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.ClientSession;
import nu.rydin.kom.frontend.text.TelnetInputStream;
import nu.rydin.kom.utils.Logger;

/**
 * A simple telnet server. Listens to a port, and kicks of
 * threads handling the sessions when an incoming connection
 * is detected.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TelnetServer implements Module, Runnable
{
	/**
	 * Cleans up after dead telnet session threads.
	 * 
	 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
	 */
	private static class SessionReaper extends Thread
	{
		private Socket m_socket;
		
		private Thread m_clientThread;
		
		private ClientSession m_session;
		
		public SessionReaper(Socket socket, Thread clientThread, ClientSession session)
		{
			super("SessionReaper");
			m_socket 		= socket;
			m_clientThread 	= clientThread;
			m_session		= session;
		}
		
		public void run()
		{
			// Wait for the client to die
			//
			try
			{
				m_clientThread.join();
				try
				{
					m_session.shutdown();
				}
				catch(UnexpectedException e)
				{
					e.printStackTrace();
				}
				Logger.info(this, "Disconnected from " + m_socket.getInetAddress().getHostAddress());
			}
			catch(InterruptedException e)
			{
				// Interruped while waiting? We're going down, so
				// just fall thru and kill the connection.
				//
			}
			finally
			{
				try
				{
					m_socket.close();
				}
				catch(IOException e)
				{
					// IO error here? Tough luck...
					//
					e.printStackTrace();
				}
				
				// Release references
				//
				m_socket 		= null;
				m_clientThread 	= null;
				m_session 		= null;
			}
		}
	}

	private ServerSocket m_socket;
	private Thread m_thread;
	private boolean m_useTicket;
	private boolean m_selfRegister;
		
	public void start(Map parameters)
	{
	    // Perform checks before start.
	    //
		int port = Integer.parseInt((String) parameters.get("port"));
		m_useTicket = "ticket".equals(parameters.get("authentication"));
		m_selfRegister = "true".equals(parameters.get("selfRegister"));
		
		try
		{
			m_socket = new ServerSocket(port);
			m_socket.setReceiveBufferSize(65536);
		}
		catch(IOException e)
		{
			// We can't even listen on the socket. Most likely, someone
			// else is already listening to that port. In any case, we're
			// out of here!
			//
			Logger.fatal(this, e);
			return;
		}
		Logger.info(this, "OpenKOM telnet server is accepting connections at port " + port);	    
		
		// Start server thread
		//
		m_thread = new Thread(this, "Telnet Server");
		m_thread.start();
	}
	
	public void stop()
	{
	    try
	    {
		    m_thread.interrupt();
		    m_socket.close();
	    }
	    catch(IOException e)
	    {
	        Logger.error(this, "Exception while shutting down", e);
	    }
	}
	
	public void join()
	throws InterruptedException
	{
	    if(m_thread != null)
	        m_thread.join();
	}
	
	public void run()
	{   
		for(;;)
		{
			try
			{
				// Wait for someone to connect
				//
				Socket incoming = m_socket.accept();
				incoming.setKeepAlive(true);
				Logger.info(this, "Incoming connection from " + incoming.getInetAddress().getHostAddress() +
				        ". Buffer size=" + incoming.getReceiveBufferSize());
				try
				{
					// Create session
					//
					TelnetInputStream eis = new TelnetInputStream(incoming.getInputStream(), 
						incoming.getOutputStream());
					ClientSession client = new ClientSession(eis, incoming.getOutputStream(), m_useTicket, m_selfRegister);
					eis.addSizeListener(client);
					eis.addEnvironmentListener(client);
					
					// Create a thread to handle the session and kick it off!
					// Also create a SessionReaper that will be woken up when the
					// session dies to perform post-mortem cleanup.
					//
					Thread clientThread = new Thread(client, "Session (not logged in)");
					Thread reaper = new SessionReaper(incoming, clientThread, client);
					reaper.start();
					clientThread.start();
				}
				catch(Exception e)
				{
					// Couldn't create session. Kill connection!
					//
					e.printStackTrace();
					incoming.close();
					continue;
				}
			}
			catch(IOException e)
			{
				// Error accepting. Not good. Try again!
				//
				e.printStackTrace();
				continue;	
			}
		}
	}
	
	public static void main(String[] args)
	{
		Logger.fatal(TelnetServer.class, "Starting TelnetServer directly is no longer supported. Use rydin.nu.kom.boot.Bootstrap instead.");
	}
}
