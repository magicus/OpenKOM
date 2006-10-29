package nu.rydin.kom.modules.email;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;

import nu.rydin.kom.utils.Logger;

public class Listener extends Thread
{
    private final int port;
    
    private final String domain;
    
    private final LinkedList pool = new LinkedList();
    
    public Listener(int port, String domain, int nWorkers)
    {
        this.port   = port;
        this.domain = domain;
    }
    
    public void run()
    {
        ServerSocket ss;
        try
        {
            ss = new ServerSocket(port);
        }
        catch(IOException e)
        {
            Logger.fatal(this, "Error listening on socket", e);
            return;
        }
        try
        {
            for(;;)
            {
                try
                {
                    Socket s = ss.accept();
                }
                catch(IOException e)
                {
                    Logger.error(this, e);
                    Thread.sleep(5000); // Wait a while before trying again
                }
            }
        }
        catch(InterruptedException e)
        {
            Logger.info(this, "Shutting down");
        }
    }
}
