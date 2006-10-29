package nu.rydin.kom.modules.email;

import java.net.Socket;

public class Worker extends Thread
{
    private final String domain;
    
    private final Socket in;
    
    public Worker(String domain, Socket in)
    {
        this.domain = domain;
        this.in     = in;
    }
}
