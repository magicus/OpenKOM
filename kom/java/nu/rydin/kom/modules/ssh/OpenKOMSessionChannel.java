/*
 * Created on Dec 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nu.rydin.kom.exceptions.InternalException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.ClientSession;
import nu.rydin.kom.frontend.text.TerminalSizeListener;
import nu.rydin.kom.utils.Logger;

import com.sshtools.j2ssh.connection.ChannelOutputStream;
import com.sshtools.j2ssh.connection.IOChannel;
import com.sshtools.j2ssh.connection.InvalidChannelException;
import com.sshtools.j2ssh.connection.SshMsgChannelExtendedData;
import com.sshtools.j2ssh.io.ByteArrayReader;

/**
 * @author Henrik Schröder
 *
 */
public class OpenKOMSessionChannel extends IOChannel
{
    public final static String SESSION_CHANNEL_TYPE = "session";
    
    private ChannelOutputStream stderrOut;
    private final List m_sizeListeners = new LinkedList();

    public byte[] getChannelOpenData()
    {
        return null;
    }

    public byte[] getChannelConfirmationData()
    {
        return null;
    }

    protected int getMinimumWindowSpace()
    {
        return 1024;
    }

    protected int getMaximumWindowSpace()
    {
        return 32648;
    }

    protected int getMaximumPacketSize()
    {
        return 32648;
    }

    public String getChannelType()
    {
        return SESSION_CHANNEL_TYPE;
    }

    protected void onChannelOpen() throws InvalidChannelException
    {
        stderrOut = new ChannelOutputStream(this, new Integer(
                SshMsgChannelExtendedData.SSH_EXTENDED_DATA_STDERR));
    }

	public void addSizeListener(TerminalSizeListener listener)
	{
		synchronized(m_sizeListeners)
		{
			m_sizeListeners.add(listener);
		}
	}
    
    protected void onChannelRequest(String requestType, boolean wantReply,
            byte[] requestData) throws IOException
    {
        if (requestType.equals("shell"))
        {
            connection.sendChannelRequestSuccess(this);
            try
            {
                String ticket = OpenKOMAuthenticationProvider.claimTicket();
                Logger.debug(this, "Ticket: " + ticket);
                ClientSession client = new ClientSession(getInputStream(), getOutputStream(), true);
                addSizeListener(client);
                client.setTicket(ticket);
                Thread clientThread = new Thread(client, "Session (not logged in)");
				clientThread.start();
            } catch (UnexpectedException e)
            {
                Logger.error(this, e);
                throw new IOException();
            } catch (InternalException e)
            {
                Logger.error(this, e);
                throw new IOException();
            }
        }
        else if (requestType.equals("pty-req"))
        {
            ByteArrayReader bar = new ByteArrayReader(requestData);
            String term = bar.readString();
            int cols = (int) bar.readInt();
            int rows = (int) bar.readInt();
            int width = (int) bar.readInt();
            int height = (int) bar.readInt();
            String modes = bar.readString();
            
            if (wantReply)
            {
                connection.sendChannelRequestSuccess(this);
            }
        }
        else if (requestType.equals("window-change"))
        {
            ByteArrayReader bar = new ByteArrayReader(requestData);
            int cols = (int) bar.readInt();
            int rows = (int) bar.readInt();
            int width = (int) bar.readInt();
            int height = (int) bar.readInt();
            
    		synchronized(m_sizeListeners)
    		{
    			for(Iterator itor = m_sizeListeners.iterator(); itor.hasNext();)
    				((TerminalSizeListener) itor.next()).terminalSizeChanged(cols, rows);
    		}

            if (wantReply)
            {
                connection.sendChannelRequestSuccess(this);
            }
        }
        else
        {
            if (wantReply)
            {
                connection.sendChannelRequestFailure(this);
            }
        }


    }

}
