/*
 * Created on Dec 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Modules;

import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.daemon.platform.PasswordChangeException;

/**
 * @author Henrik Schröder
 *  
 */
public class OpenKOMAuthenticationProvider extends NativeAuthenticationProvider
{

    /**
     *  
     */
    public OpenKOMAuthenticationProvider()
    {
        super();
    }

    public String getHomeDirectory(String username) throws IOException
    {
        //???
        return "/home/foo/bar";
    }

    public boolean logonUser(String username, String password)
            throws PasswordChangeException, IOException
    {
        try
        {
            ((ServerSessionFactory) Modules.getModule("Backend")).authenticate(
                    username, password);
            return true;
        } catch (AuthenticationException e)
        {
        } catch (UnexpectedException e)
        {
        } catch (NoSuchModuleException e)
        {
        }
        return false;
    }

    public boolean logonUser(String username) throws IOException
    {
        //No way.
        return false;
    }

    public void logoffUser() throws IOException
    {
        //???
    }

    public boolean changePassword(String username, String oldpassword,
            String newpassword)
    {
        //No way.
        return false;
    }

}