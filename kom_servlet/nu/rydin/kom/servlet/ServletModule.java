/*
 * Created on Sep 28, 2005
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.servlet;

import java.util.Map;

import org.mortbay.jetty.Server;

import nu.rydin.kom.exceptions.ModuleException;
import nu.rydin.kom.modules.Module;
import nu.rydin.kom.utils.Logger;

/**
 * @author Pontus Rydin
 */
public class ServletModule implements Module
{
    private Server servletEngine;
    
    public void start(Map parameters) throws ModuleException
    {
        try
        {
	        String home = (String) parameters.get("jetty.home");
	        System.setProperty("jetty.home", home);
	        String config = (String) parameters.get("config");
	        if(config == null)
	            config = home + "/etc/jetty.xml";
	        this.servletEngine = new Server(config);
	        servletEngine.start();
        }
        catch(Exception e)
        {
            throw new ModuleException(e);
        }
    }

    public void stop()
    {
        try
        {
            this.servletEngine.stop();
        }
        catch(InterruptedException e)
        {
            Logger.warn(this, "Interrupted while stopping servlet engine. Ignoring!");
        }
    }

    public void join() 
    throws InterruptedException
    {
        this.servletEngine.join();
    }
}
