/*
 * Created on Dec 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelFactory;
import com.sshtools.j2ssh.connection.InvalidChannelException;

/**
 * This factory class provides the J2SSH server with a channel implementation
 * that handle the channel type "session", which is the standard channel
 * for interactive shell access
 * 
 * @author Henrik Schröder
 */
public class OpenKOMSessionChannelFactory implements ChannelFactory
{

    public final static String SESSION_CHANNEL = "session";

    Class sessionChannelImpl;

    public OpenKOMSessionChannelFactory() throws ConfigurationException
    {
        sessionChannelImpl = OpenKOMSessionChannel.class;
    }

    public Channel createChannel(String channelType, byte[] requestData)
            throws InvalidChannelException
    {
        try
        {
            if (channelType.equals("session"))
            {
                return (Channel) sessionChannelImpl.newInstance();
            } 
            else
            {
                throw new InvalidChannelException(
                        "Only session channels can be opened by this factory");
            }
        } 
        catch (Exception e)
        {
            throw new InvalidChannelException(
                    "Failed to create session channel implemented by "
                            + sessionChannelImpl.getName());
        }
    }
}