/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import nu.rydin.kom.UnexpectedException;

/**
 * A simple telnet server. Listens to a port, and kicks of
 * threads handling the sessions when an incoming connection
 * is detected.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class TelnetServer implements Runnable
{
	/**
	 * Cleans up after dead telnet session threads.
	 * 
	 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
	 */
	private static class SessionReaper extends Thread
	{
		private final Socket m_socket;
		
		private final Thread m_clientThread;
		
		private final ClientSession m_session;
		
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
				System.out.println("Disconnected from " + m_socket.getInetAddress().getHostAddress());
			}
			catch(InterruptedException e)
			{
				// Interruped while waiting? We're going down, so
				// just fall thru and kill the connection.
				//
			}
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
		}
	}
	private int m_port;
	
	public void run()
	{
		// Before we do anything, make sure we're even able to start
		//
		if(!this.sanityChecks())
		{
			System.err.println("FATAL: Cannot start server due to the above errors.");
			System.exit(1);
		}
		
		ServerSocket s = null;
		try
		{
			s = new ServerSocket(ClientSettings.getTelnetPort());
		}
		catch(IOException e)
		{
			// We can't even listen on the socket. Most likely, someone
			// else is already listening to that port. In any case, we're
			// out of here!
			//
			e.printStackTrace();
			return;
		}
		System.out.println("OpenKOM telnet server is accepting connections at port " + 
			ClientSettings.getTelnetPort());
		for(;;)
		{
			try
			{
				// Wait for someone to connect
				//
				Socket incoming = s.accept();
				System.out.println("Incoming connection from " + incoming.getInetAddress().getHostAddress());
				try
				{
					// Create session
					//
					TelnetInputStream eis = new TelnetInputStream(incoming.getInputStream(), 
						incoming.getOutputStream());
					ClientSession client = new ClientSession(eis, incoming.getOutputStream());
					eis.addSizeListener(client);
					
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
		new TelnetServer().run();
	}
	
	protected boolean sanityChecks()
	{
		boolean ok = true;
		// Check that we have the character sets we need
		//
		StringTokenizer st = new StringTokenizer(ClientSettings.getCharsets(), ",");
		while(st.hasMoreTokens())
		{
			String charSet = st.nextToken();
			try
			{
				new OutputStreamWriter(System.out, charSet);
			}
			catch(UnsupportedEncodingException e)
			{
				System.err.println("Character set " + charSet + " not supported. Do you have charsets.jar in you classpath?");
				ok = false;					
			}
		}
		return ok;
	}
}
