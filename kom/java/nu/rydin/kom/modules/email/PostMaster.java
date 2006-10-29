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
    private Listener worker;
    
    public void start(Map parameters) 
    throws ModuleException
    {
        worker = new Listener(
                Integer.parseInt((String) parameters.get("port")),
                (String) parameters.get("domain"),
                Integer.parseInt((String) parameters.get("numWorkers")));
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
