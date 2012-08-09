/*
 * Created on Dec 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.utils.Logger;

import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.daemon.platform.PasswordChangeException;
import com.sshtools.j2ssh.SshThread;

/**
 * This is J2SSH Authentication Provider for OpenKOM. It is responsible for handling
 * all authentication requests to our SSH Server. If a user is authenticated,
 * a ticket is generated and stored. The client session can retrieve this ticket
 * at a later point as proof of successful authentication.
 * 
 * @author Henrik Schröder
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class OpenKOMAuthenticationProvider extends NativeAuthenticationProvider
{
    private static final HashMap<String, String> s_tickets = new HashMap<String, String>();
    
    private static final Pattern threadNamePattern = Pattern.compile(".*\\s([0-9a-f]+)$");
    
    public OpenKOMAuthenticationProvider()
    {
        super();
    }
    
    public static void postTicket(String ticket)
    {
        synchronized(s_tickets)
        {
            // Use the session id as key
            //
            s_tickets.put(((SshThread) Thread.currentThread()).getSessionIdString(), ticket);
        }
    }
    
    public static String claimTicket()
    {
        synchronized(s_tickets)
        {
            // Use the session id as key
            //
            return (String) s_tickets.remove(((SshThread) Thread.currentThread()).getSessionIdString());
        }
    }

    public String getHomeDirectory(String username) throws IOException
    {
        // We don't provide a home directory.
        //
        return "/no/way/jose";
    }

    public boolean logonUser(String username, String password)
            throws PasswordChangeException, IOException
    {
        // Dept. of disgusting hacks.. This is the only way we can get the transport protocol ID.
        //
        SSHServer server = null;
        try
        {
            server =  (SSHServer) Modules.getModule("SSHModule");
        }
        catch (NoSuchModuleException nsme)
        {
            Logger.fatal(this, "PANIC: The module manager didn't find the SSH module");
            return false;
        }

        int protId = 0;
        try
        {
            SshThread t = ((SshThread)Thread.currentThread());
            Matcher m = threadNamePattern.matcher(t.getName());
            if(m.matches())
            {
                String s = m.group(1);
                Logger.debug(this, "Parsed thread name and found id=" + s);
                protId = Integer.parseInt(s, 16); // Yes, the number is in hex!
            }
            else
            {
                Logger.warn(this, "Unable to parse thread name: " + t);
            }
        }
        catch (NumberFormatException e)
        {
            Logger.warn(this, "parseInt() failed in logonUser()", e);
        }
        
        String userAtHost = username + "@" + server.getClientFromProtocolNumber(protId);
        
        try
        {
            Logger.info(this, "Trying to log in as: " + userAtHost);
            String ticket = ((ServerSessionFactory) Modules.getModule("Backend")).generateTicket(
                    username, password);
            postTicket(ticket);
            Logger.info(this, "Successfully authenticated as: " + userAtHost);
            
            // Notify the SSH server
            //
            server.notifyLogonStatus(protId, true);

            return true;
        } catch (AuthenticationException e)
        {
            // Just fall thru and return false
        } catch (UnexpectedException e)
        {
            Logger.error("Unexpected Error", e);
        } catch (NoSuchModuleException e)
        {
            Logger.fatal(this, "PANIC: Error finding required module", e);
        }
        Logger.info(this, "Failed login for " + userAtHost);
        
        try
        {
            ((SSHServer)Modules.getModule("SSHModule")).notifyLogonStatus(protId, false);
        }
        catch (NoSuchModuleException e)
        {
            Logger.warn (this, "Could not locate SSH module");
        }
        return false;
    }

    public boolean logonUser(String username) throws IOException
    {
        // We do not support this.
        //
        return false;
    }

    public void logoffUser() throws IOException
    {
        //???
    }

    public boolean changePassword(String username, String oldpassword,
            String newpassword)
    {
        // We do not support this.
        //
        return false;
    }
}