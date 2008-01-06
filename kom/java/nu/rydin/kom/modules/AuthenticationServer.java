/*
 * Created on Sep 17, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class AuthenticationServer implements Module, Runnable
{
    private static class AuthenticationWorker extends Thread
    {
        private Socket m_sock;
        
        public AuthenticationWorker(Socket sock)
        {
            m_sock = sock;
        }
        
        public void run()
        {
            // We have an incoming call. Go ahead and process it
            //
            try
            {
	            BufferedReader rdr = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
	            PrintStream out = new PrintStream(m_sock.getOutputStream());
	            
	            // Read username and password from client
	            //
	            String user = rdr.readLine();
	            String password = rdr.readLine();
	            
	            // We have authentication info. Go authenticate
	            //
	            try
	            {
	                String ticket = ((ServerSessionFactory) Modules.getModule("Backend")).generateTicket(user, password);
	                out.print("OK:");
	                out.print(ticket);
	                out.print("\r\n");
	            }
	            catch(AuthenticationException e)
	            {
	                // No go!
	                //
	                out.print("FAIL\r\n");
	            }
	            out.flush();
	            
            }
			catch(Exception e)
			{
				// Something went terribly wrong!
				//
				Logger.error(this, e);
			}
			finally
			{
			    try
			    {
			        m_sock.close();
			    }
			    catch(IOException e)
			    {
			        Logger.error(this, "Error closing socket", e);
			    }
			}
        }
    }
    
    private Thread m_thread;
    
    private ServerSocket m_socket;
    
    public void start(Map<String, String> parameters)
    {
        int port = Integer.parseInt((String) parameters.get("port"));
		try
		{
			m_socket = new ServerSocket(port);
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
		Logger.info(this, "OpenKOM authentication server is accepting connections at port " + port);

        m_thread = new Thread(this, "AuthenticationServer");
        m_thread.start();
    }

    public void stop()
    {
        m_thread.interrupt();
    }

    public void join() 
    throws InterruptedException
    {
        m_thread.join();
    }
    
    public void run()
    {
        for(;;)
        {
	        try
	        {
	            Socket sock = m_socket.accept();
	            
	            // We have an incoming call. Go ahead and process it in a separate thread
	            //
	            AuthenticationWorker worker = new AuthenticationWorker(sock);
	            worker.start();
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

}
