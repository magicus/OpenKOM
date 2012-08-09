/*
 * Created on Dec 10, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;
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
 * This is the OpenKOM implementation of a an SSH session channel.
 * 
 * SSH allows for concurrent, multiplexed, encrypted channels over 
 * one connection, once it has been authenticated. We are only interested
 * in the channel called "session", and our SSH server only implements
 * a handler for channels of this type, and this is the class for it.
 * 
 * On a channel, the client and server can either send data or requests. 
 * 
 * The IOCHannel superclass offers us an InputStream and an OutputStream which
 * transparently handle all data traffic in the channel.
 * 
 * However, we still have to handle channel requests. We don't care or handle
 * all of the requests that are in the specification, but we do handle the 
 * following:
 * 
 * pty-req: The client requests a pseudo-terminal of a given terminal-type 
 * and size.
 * 
 * window-change: The client changes the size of the pseudo-terminal to the 
 * given size.
 * 
 * shell: The client wants to start an interactive shell. We give it OpenKOM.
 * 
 * @author Henrik Schröder
 */
public class OpenKOMSessionChannel extends IOChannel
{
    public final static String SESSION_CHANNEL_TYPE = "session";
    
    @SuppressWarnings("unused")
    private ChannelOutputStream stderrOut;
    private final List<TerminalSizeListener> m_sizeListeners = new LinkedList<TerminalSizeListener>();
    
    private int ptyColumns = 80;
    private int ptyRows = 25;
    private final SSHServer server;
    private final String clientName;
    
    public OpenKOMSessionChannel(final SSHServer server, String clientName)
    {
        super();
        this.server = server;
        this.clientName = clientName;
    }
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

    protected void onChannelClose() throws IOException
    {
        Logger.debug(this, "ON CHANNEL CLOSE");
        super.onChannelClose();
    }
    protected void onChannelEOF() throws IOException
    {
        Logger.debug(this, "ON CHANNEL EOF");
        super.onChannelEOF();
    }

	private void addSizeListener(TerminalSizeListener listener)
	{
		synchronized(m_sizeListeners)
		{
			m_sizeListeners.add(listener);
		}
	}
    
	private static class SessionReaper extends Thread
	{	
	    private OpenKOMSessionChannel m_channel;
		private Thread m_clientThread;
		private ClientSession m_session;
		
		public SessionReaper(OpenKOMSessionChannel channel, Thread clientThread, ClientSession session)
		{
			super("SessionReaper");
			m_channel = channel;
			m_clientThread 	= clientThread;
			m_session		= session;
		}
		
		public void run()
		{
			// Wait for the client to die
			//
			try
			{
				m_clientThread.join();
				try
				{
					m_session.shutdown();
				}
				catch(UnexpectedException e)
				{
					Logger.warn(this, "Error while reaping session: " + e.getMessage());
                }
				try
				{
                    m_channel.setLocalEOF();
                    m_channel.setRemoteEOF();
                    m_channel.close();
				}
				catch (IOException e1)
                {
                    Logger.warn(this, "Error while reaping session: " + e1.getMessage());
                }
			}
			catch(InterruptedException e)
			{
				// Interruped while waiting? We're going down, so
				// just fall thru and kill the connection.
				//
			}
			finally
			{
				// Release references
				//
				m_clientThread 	= null;
				m_session 		= null;
			}
			Logger.debug(this, "Closing channel");
		}
	}
	
    protected void onChannelRequest(String requestType, boolean wantReply,
            byte[] requestData) throws IOException
    {
        if (requestType.equals("shell"))
        {
            // This request is issued by the client if it wants an interactive shell
            // on the server. Our business is to provide OpenKOM as that shell, so we'll
            // gladly accept this and start a ClientSession
            //
            try
            {
                String ticket = OpenKOMAuthenticationProvider.claimTicket();
                Logger.debug(this, "Ticket: " + ticket);
                ClientSession client = new ClientSession(getInputStream(), getOutputStream(), true, false, clientName, server.getParameters());
                addSizeListener(client);
                client.setTicket(ticket);
                Thread clientThread = new Thread(client, "Session (not logged in)");
				Thread reaper = new SessionReaper(this, clientThread, client);
				reaper.start();
				clientThread.start();
				client.terminalSizeChanged(ptyColumns, ptyRows);
				
				if (wantReply)
				{
				    connection.sendChannelRequestSuccess(this);
				}
				
            } catch (UnexpectedException e)
            {
                Logger.error(this, e);
				if (wantReply)
				{
				    connection.sendChannelRequestFailure(this);
				}
				//I think a request-failure is enough here.
                //throw new IOException();
            } catch (InternalException e)
            {
                Logger.error(this, e);
				if (wantReply)
				{
				    connection.sendChannelRequestFailure(this);
				}
				//I think a request-failure is enough here.
                //throw new IOException();
            }
        }
        else if (requestType.equals("pty-req"))
        {
            // This request is recieved when the client wants a pseudo-terminal,
            // and it should be issued by the client before a shell or exec request.
            //
            
            ByteArrayReader bar = new ByteArrayReader(requestData);
            String term = bar.readString();
            int cols = (int) bar.readInt();
            int rows = (int) bar.readInt();
            @SuppressWarnings("unused")
            int width = (int) bar.readInt();
            @SuppressWarnings("unused")
            int height = (int) bar.readInt();
            @SuppressWarnings("unused")
            String modes = bar.readString();
            
            Logger.debug(this, "Requested pty-req with " + term + " of " + cols + "x" + rows);
            
            if (cols > 0 && rows > 0)
            {
                ptyColumns = cols;
                ptyRows = rows;
            }
            
            if (wantReply)
            {
                connection.sendChannelRequestSuccess(this);
            }
        }
        else if (requestType.equals("window-change"))
        {
            // This request is recieved every time the client terminal changes size
            //

            ByteArrayReader bar = new ByteArrayReader(requestData);
            int cols = (int) bar.readInt();
            int rows = (int) bar.readInt();
            @SuppressWarnings("unused")
            int width = (int) bar.readInt();
            @SuppressWarnings("unused")
            int height = (int) bar.readInt();
            
            Logger.debug(this, "Requested window-change to " + cols + "x" + rows);
            
            if (cols > 0 && rows > 0)
            {
	    		synchronized(m_sizeListeners)
	    		{
	    		    for (TerminalSizeListener listener : m_sizeListeners)
                    {
                        listener.terminalSizeChanged(cols, rows);
                    }
	    		}
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
