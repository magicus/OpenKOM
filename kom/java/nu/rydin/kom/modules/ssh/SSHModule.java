/*
 * Created on Nov 10, 2003
 * 
 * Distributed under the GPL license. See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import nu.rydin.kom.frontend.text.ClientSettings;
import nu.rydin.kom.modules.Module;
import nu.rydin.kom.utils.Logger;

import com.sshtools.daemon.authentication.AuthenticationProtocolServer;
import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.daemon.transport.TransportProtocolServer;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.SshThread;
import com.sshtools.j2ssh.configuration.ConfigurationContext;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.net.ConnectedSocketTransportProvider;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.TransportProtocolEventAdapter;
import com.sshtools.j2ssh.transport.TransportProtocolEventHandler;
import com.sshtools.j2ssh.util.StartStopState;

public class SSHModule implements Module, Runnable
{
    InnerJ2SSHServer myserver;

    private static class InnerJ2SSHServer
    {
        private ConnectionListener listener = null;

        public InnerJ2SSHServer() throws IOException
        {
            String serverId = System.getProperty("sshtools.serverid");

            if (serverId != null)
            {
                TransportProtocolServer.SOFTWARE_VERSION_COMMENTS = serverId;
            }

            if (!ConfigurationLoader
                    .isConfigurationAvailable(ServerConfiguration.class))
            {
                throw new SshException("Server configuration not available!");
            }

            if (!ConfigurationLoader
                    .isConfigurationAvailable(PlatformConfiguration.class))
            {
                throw new SshException("Platform configuration not available");
            }

            if (((ServerConfiguration) ConfigurationLoader
                    .getConfiguration(ServerConfiguration.class))
                    .getServerHostKeys().size() <= 0)
            {
                throw new SshException(
                        "Server cannot start because there are no server host keys available");
            }
        }

        public void configureServices(ConnectionProtocol connection)
                throws IOException
        {
            connection.addChannelFactory(
                    OpenKOMSessionChannelFactory.SESSION_CHANNEL,
                    new OpenKOMSessionChannelFactory());
        }

        public void shutdown(String msg)
        {
            //TODO: Implement?
            // Disconnect all sessions
        }

        protected List activeConnections = new Vector();

        Thread thread;

        protected void startServerSocket() throws IOException
        {
            listener = new ConnectionListener(
                    ((ServerConfiguration) ConfigurationLoader
                            .getConfiguration(ServerConfiguration.class))
                            .getListenAddress(),
                    ((ServerConfiguration) ConfigurationLoader
                            .getConfiguration(ServerConfiguration.class))
                            .getPort());
            listener.start();
        }

        public void stopServer(String msg) throws IOException
        {
            Logger.info(this, "Shutting down server");
            Logger.debug(this, msg);
            shutdown(msg);
            listener.stop();
        }

        protected void refuseSession(Socket socket) throws IOException
        {
            TransportProtocolServer transport = new TransportProtocolServer(true);
            transport.startTransportProtocol(
                    new ConnectedSocketTransportProvider(socket),
                    new SshConnectionProperties());
        }

        protected TransportProtocolServer createSession(Socket socket)
                throws IOException
        {
            Logger.debug(this, "Initializing connection");

            InetAddress address = socket.getInetAddress();

            /*
             * ( (InetSocketAddress) socket
             * .getRemoteSocketAddress()).getAddress();
             */
            Logger.debug(this, "Remote Hostname: " + address.getHostName());
            Logger.debug(this, "Remote IP: " + address.getHostAddress());

            TransportProtocolServer transport = new TransportProtocolServer();

            // Create the Authentication Protocol
            AuthenticationProtocolServer authentication = new AuthenticationProtocolServer();

            // Create the Connection Protocol
            ConnectionProtocol connection = new ConnectionProtocol();

            // Configure the connections services
            configureServices(connection);

            // Allow the Connection Protocol to be accepted by the
            // Authentication Protocol
            authentication.acceptService(connection);

            // Allow the Authentication Protocol to be accepted by the Transport
            // Protocol
            transport.acceptService(authentication);
            transport.startTransportProtocol(
                    new ConnectedSocketTransportProvider(socket),
                    new SshConnectionProperties());

            return transport;
        }

        class ConnectionListener implements Runnable
        {
            private ServerSocket server;
            private String listenAddress;
            private Thread thread;
            private int maxConnections;
            private int port;

            private StartStopState state = new StartStopState(
                    StartStopState.STOPPED);

            public ConnectionListener(String listenAddress, int port)
            {
                this.port = port;
                this.listenAddress = listenAddress;
            }

            public void run()
            {
                try
                {
                    Logger.debug(this, "Starting connection listener thread");
                    state.setValue(StartStopState.STARTED);
                    server = new ServerSocket(port);

                    Socket socket;
                    maxConnections = ((ServerConfiguration)ConfigurationLoader
                            .getConfiguration(ServerConfiguration.class))
                            .getMaxConnections();

                    TransportProtocolEventHandler eventHandler = new TransportProtocolEventAdapter()
                    {
                        public void onDisconnect(TransportProtocol transport)
                        {
                            // Remove from our active channels list only if
                            // were still connected (the thread cleans up
                            // when were exiting so this is to avoid any
                            // concurrent
                            // modification problems
                            if (state.getValue() != StartStopState.STOPPED)
                            {
                                synchronized (activeConnections)
                                {
                                    Logger.info(this, transport.getUnderlyingProviderDetail()
                                            + " connection closed");
                                    activeConnections.remove(transport);
                                }
                            }
                        }
                    };

                    try
                    {
                        while (((socket = server.accept()) != null)
                                && (state.getValue() == StartStopState.STARTED))
                        {
                            Logger.debug(this, "New connection requested");

                            if (maxConnections < activeConnections.size())
                            {
                                refuseSession(socket);
                            } else
                            {
                                TransportProtocolServer transport = createSession(socket);
                                Logger.info(this, "Monitoring active session from "
                                                + socket.getInetAddress().getHostName());

                                synchronized (activeConnections)
                                {
                                    activeConnections.add(transport);
                                }

                                transport.addEventHandler(eventHandler);
                            }
                        }
                    } catch (IOException ex)
                    {
                        if (state.getValue() != StartStopState.STOPPED)
                        {
                            Logger.info(this, "The server was shutdown unexpectedly", ex);
                        }
                    }

                    state.setValue(StartStopState.STOPPED);

                    // Closing all connections
                    Logger.info(this, "Disconnecting active sessions");

                    for (Iterator it = activeConnections.iterator(); it.hasNext();)
                    {
                        ((TransportProtocolServer)it.next()).disconnect("The server is shutting down");
                    }

                    listener = null;
                    Logger.info(this, "Exiting connection listener thread");
                } catch (IOException ex)
                {
                    Logger.info(this, "The server thread failed", ex);
                } finally
                {
                    thread = null;
                }

                // brett
                //      System.exit(0);
            }

            public void start()
            {
                thread = new SshThread(this, "Connection listener", true);
                thread.start();
            }

            public void stop()
            {
                try
                {
                    state.setValue(StartStopState.STOPPED);
                    server.close();

                    if (thread != null)
                    {
                        thread.interrupt();
                    }
                } catch (IOException ioe)
                {
                    Logger.warn(this, "The listening socket reported an error during shutdown", ioe);
                }
            }
        }
    }

    public void start(Map parameters)
    {
        // Before we do anything, make sure we're even able to start
        //
        if (!this.sanityChecks())
        {
            Logger.fatal(this, "FATAL: Cannot start SSH server due to the above errors.");
            return;
        }

        try
        {
            ConfigurationContext dummy = new DummyConfigurationContext(
                    (String) parameters.get("serverhostkeyfile"), 
                    Integer.parseInt((String)parameters.get("port")), 
                    Integer.parseInt((String)parameters.get("maxauthretry")),
                    Integer.parseInt((String)parameters.get("maxconn")));
            ConfigurationLoader.initialize(true, dummy);
        } catch (ConfigurationException e)
        {
            Logger.fatal(this, "Cannot start SSH Server due to flawed configuration.", e);
        }

        try
        {
            myserver = new InnerJ2SSHServer();
            myserver.startServerSocket();
        } catch (IOException e)
        {
            Logger.fatal(this, "Cannot start SSH Server: " + e.getMessage(), e);
        }

        Logger.info(this, "OpenKOM SSH server is accepting connections at port "
                        + (String) parameters.get("port"));
    }

    public void stop()
    {
        try
        {
            myserver.stopServer("Shutting down");
        } catch (IOException e)
        {
            Logger.fatal(this, "Cannot stop SSH Server: " + e.getMessage(), e);
        }
    }

    public void join() throws InterruptedException
    {
        //???
    }

    public void run()
    {
        //???
        for (;;)
        {
            try
            {
                Thread.sleep(10000);
            } catch (InterruptedException e)
            {
            }
        }
    }

    protected boolean sanityChecks()
    {
        boolean ok = true;

        // Check that we have the character sets we need
        //
        StringTokenizer st = new StringTokenizer(ClientSettings.getCharsets(), ",");
        while (st.hasMoreTokens())
        {
            String charSet = st.nextToken();
            try
            {
                new OutputStreamWriter(System.out, charSet);
            } catch (UnsupportedEncodingException e)
            {
                Logger.fatal( this, "Character set " + charSet + " not supported. Do you have charsets.jar in you classpath?");
                ok = false;
            }
        }
        return ok;
    }
}

