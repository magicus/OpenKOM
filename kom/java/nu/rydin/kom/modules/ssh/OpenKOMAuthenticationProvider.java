/*
 * Created on Dec 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;
import java.util.HashMap;

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
 * @author Pontus Rydin
 */
public class OpenKOMAuthenticationProvider extends NativeAuthenticationProvider
{
    private static final HashMap s_tickets = new HashMap();
    
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
        try
        {
            Logger.info(this, "Trying to log in as: " + username);
            String ticket = ((ServerSessionFactory) Modules.getModule("Backend")).generateTicket(
                    username, password);
            postTicket(ticket);
            Logger.info(this, "Successfully logged in as: " + username);
            return true;
        } catch (AuthenticationException e)
        {
        } catch (UnexpectedException e)
        {
        } catch (NoSuchModuleException e)
        {
        }
        Logger.info(this, "Failed login");
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