package nu.rydin.kom.modules.ssh;

import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelFactory;
import com.sshtools.j2ssh.connection.InvalidChannelException;

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