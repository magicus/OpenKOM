/*
 * Created on Oct 25, 2006
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */

package nu.rydin.kom.modules.email;

import java.util.Map;

import nu.rydin.kom.exceptions.ModuleException;
import nu.rydin.kom.modules.Module;

/**
 * @author Pontus Rydin
 */
public class PostMaster implements Module
{
    private POP3Poller worker;
    
    public void start(Map<String, String> parameters) 
    throws ModuleException
    {
        worker = new POP3Poller(
                parameters.get("host"),
                Integer.parseInt(parameters.get("port")),
                parameters.get("user"),
                parameters.get("password"),
                parameters.get("postmaster"),
                parameters.get("postmasterPassword"),
                Integer.parseInt(parameters.get("pollDelay")) * 60000,
                parameters.get("deadLetterArea"), 
                Long.parseLong(parameters.get("systemMessageConf")));
        worker.start();
    }

    public void stop()
    {
        worker.interrupt();
    }

    public void join() throws InterruptedException
    {
        worker.join();
    }

}
