/*
 * Created on Jul 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface DisplayController 
{
    public void prompt();
    
    public void messageHeader();
    
    public void messageBody();
    
    public void messageFooter();
    
    public void chatMessage();
    
    public void broadcastMessage();
    
    public void normal();
    
    public void highlight();
}