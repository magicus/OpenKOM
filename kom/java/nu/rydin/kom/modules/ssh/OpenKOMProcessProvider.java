/*
 * Created on Dec 6, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import nu.rydin.kom.utils.Logger;

import com.sshtools.daemon.platform.NativeProcessProvider;
import com.sshtools.j2ssh.io.DynamicBuffer;

/**
 * @author Henrik Schröder
 *  
 */
public class OpenKOMProcessProvider extends NativeProcessProvider
{

    private static String message = "This server does not provide shell access";

    DynamicBuffer stdin = new DynamicBuffer();

    DynamicBuffer stderr = new DynamicBuffer();

    DynamicBuffer stdout = new DynamicBuffer();

    public InputStream getInputStream() throws IOException
    {
        return stdin.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException
    {
        return stdout.getOutputStream();
    }

    public InputStream getStderrInputStream()
    {
        return stderr.getInputStream();
    }

    public boolean createProcess(String command, Map environment)
            throws IOException
    {
        // Of course this could be used to provide a wealth of features
        // according to the executed command and the environment
        return true;
    }

    public void start() throws IOException
    {
        Logger
                .fatal(this,
                        "-------------------------Init ProcessProvider-----------------------");
        stdin.getOutputStream().write(message.getBytes());

    }

    public String getDefaultTerminalProvider()
    {
        return "UnsupportShell";
    }

    public boolean supportsPseudoTerminal(String term)
    {
        return true;
    }

    public boolean allocatePseudoTerminal(String term, int cols, int rows,
            int width, int height, String modes)
    {
        Logger.debug(this, "AllocatePseudoTerminal: " + term + ":" + cols + "x"
                + rows + ".");
        return true;
    }

    public void kill()
    {
        try
        {
            stdin.close();
        } catch (Exception ex)
        {
        }
        try
        {
            stdout.close();
        } catch (Exception ex1)
        {
        }
        try
        {
            stderr.close();
        } catch (Exception ex2)
        {
        }
    }

    public boolean stillActive()
    {
        try
        {
            return stdin.getInputStream().available() > 0;
        } catch (IOException ex)
        {
            return false;
        }
    }

    public int waitForExitCode()
    {
        try
        {
            while (stdin.getInputStream().available() > 0)
            {
                try
                {
                    Thread.sleep(1000); // Crude but necessary
                } catch (InterruptedException ex)
                {
                }
            }
        } catch (IOException ex1)
        {
        }
        return 0;
    }
}