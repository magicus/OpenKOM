/*
 * Created on Dec 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.modules.Module;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.structs.IntrusionAttempt;
import nu.rydin.kom.utils.Logger;

import com.sshtools.daemon.authentication.AuthenticationProtocolServer;
import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.daemon.transport.TransportProtocolServer;
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

/**
 * This is a J2SSH server implemented as an OpenKOM module.
 * 
 * @author Henrik Schröder
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class SSHServer implements Module
{    
    private Map<String, String> parameters; 
    
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    // This is where it gets interesting. Since we're not about to fiddle with J2SSH unless we
    // absolutely have to, we'll try to make do with what's available from the outside. The only thing
    // shared between the SSH server and the auth provider is the transport protocol number, so we have
    // to add a map to convert between protocol and host in this class. Yes, this is dirty. Yes, I welcome
    // other solutions. No, I don't think there's a better way of doing it, unless someone wants to
    // override every J2SSH class from here to the auth module and pass the IP.
    //
    private Map<Integer, String> protocolHostXref = 
        Collections.synchronizedMap (new HashMap<Integer, String>());
    
    public String getClientFromProtocolNumber(int protocolNo)
    {
        return protocolHostXref.get(protocolNo);
    }

    // ... and now, something we can call from the authentication provider to notify us of login status.
    //
    public void notifyLogonStatus (int protocolNo, boolean succeeded)
    {
        String host = protocolHostXref.get(protocolNo);
        if (null == host)
        {
            // This can't happen, so it's guaranteed to..
            //
            Logger.warn(this, "notifyLogonStatus() called for non-existent transport protocol "+ protocolNo);
            return;
        }
        try
        {
            ServerSessionFactory ssf = (ServerSessionFactory) Modules.getModule("Backend");
            if(succeeded)
                ssf.notifySuccessfulLogin(host);
            else
                ssf.notifyFailedLogin(host, allowedAttempts, lockoutTime);
        }
        catch(NoSuchModuleException e)
        {
            throw new RuntimeException("Missing server backend!", e);
        }
    }
        
    private ConnectionListener listener = null;
    private boolean selfRegister;
    private long lockoutTime = 600000;
    private int allowedAttempts = 9;
    
    public void configureServices(ConnectionProtocol connection, String clientName)
            throws IOException
    {
        connection.addChannelFactory(
                OpenKOMSessionChannelFactory.SESSION_CHANNEL,
                new OpenKOMSessionChannelFactory(this, clientName));
    }

    protected List<TransportProtocolServer> activeConnections = new Vector<TransportProtocolServer>();
    
    public boolean allowsSelfRegister()
    {
        return selfRegister;
    }

    protected TransportProtocolServer createSession(Socket socket)
            throws IOException
    {
        Logger.debug(this, "Initializing connection");

        InetAddress address = socket.getInetAddress();
        // The reverse lookup here can cause problems. We're disabling it for now.
        //
//        Logger.debug(this, "Remote Hostname: " + address.getHostName());
        Logger.info(this, "Attempting to create SSH session. Remote IP: " + address.getHostAddress());

        Logger.debug(this, "Socket keepalive: " + socket.getKeepAlive());        
        socket.setKeepAlive(true);
        Logger.debug(this, "Socket keepalive: " + socket.getKeepAlive());
        
        TransportProtocolServer transport = new TransportProtocolServer();
        
        // Add this session to the map
        //
        String clientName = socket.getInetAddress().getHostAddress();
        try
        {
            this.protocolHostXref.put(transport.getConnectionId(), socket.getInetAddress().getHostAddress());
        }
        catch (Exception e)
        {
            Logger.warn (this, "Couldn't add protocol-host pair to server map", e);
        }

        // Create the Authentication Protocol
        AuthenticationProtocolServer authentication = new AuthenticationProtocolServer();

        // Create the Connection Protocol
        ConnectionProtocol connection = new ConnectionProtocol();

        // Configure the connections services
        configureServices(connection, clientName);

        // Allow the Connection Protocol to be accepted by the
        // Authentication Protocol
        authentication.acceptService(connection);

        // Allow the Authentication Protocol to be accepted by the Transport
        // Protocol
        transport.acceptService(authentication);
        transport.startTransportProtocol(new ConnectedSocketTransportProvider(
                socket), new SshConnectionProperties());

        return transport;
    }

    class ConnectionListener implements Runnable
    {
        private ServerSocket server;
        private Thread thread;
        private int maxConnections;
        private int port;
        private StartStopState state = new StartStopState(
                StartStopState.STOPPED);

        public ConnectionListener(int port)
        {
            this.port = port;
        }

        public void run()
        {
            try
            {
                ServerSessionFactory ssf = (ServerSessionFactory) Modules.getModule("Backend");
                Logger.debug(this, "Starting connection listener thread");
                state.setValue(StartStopState.STARTED);
                server = new ServerSocket(port);

                Socket socket;
                maxConnections = ((ServerConfiguration) ConfigurationLoader
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
                                Logger.info(this, transport
                                        .getUnderlyingProviderDetail()
                                        + " connection closed");
                                activeConnections.remove(transport);
                                protocolHostXref.remove(transport.getConnectionId());
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
                        
                        /// Check if we're blacklisted
                        //
                        String client = socket.getInetAddress().getHostAddress();
                        if(ssf.isBlacklisted(client))
                        {
                            Logger.info(this, "Refusing connection from blacklisted host: " + client);
                            socket.close();
                            continue;
                        }
                        
                        // Check if we're out of connections
                        //
                        if(maxConnections < activeConnections.size())
                        {
                            Logger.info(this, "Refusing connection because maximum number of connections has been exceeded. Client: " + client);
                            socket.close();
                            continue;
                        }
                        
                        // We're not blacklisted, go on and accept the connection!
                        //
        				Logger.info(this, "Incoming connection from " 
        				        + socket.getInetAddress().getHostAddress() + ".");
                        TransportProtocolServer transport = createSession(socket);
                        
                        synchronized (activeConnections)
                        {
                            activeConnections.add(transport);
                        }

                        transport.addEventHandler(eventHandler);
                    }
                } catch (IOException ex)
                {
                    if (state.getValue() != StartStopState.STOPPED)
                    {
                        Logger.warn(this,
                                "The server was shutdown unexpectedly", ex);
                    }
                }

                state.setValue(StartStopState.STOPPED);

                // Closing all connections
                Logger.info(this, "Disconnecting active sessions");

                for (TransportProtocolServer connection : activeConnections)
                {
                    connection.disconnect("The server is shutting down");
                }

                listener = null;
                Logger.info(this, "Exiting server thread");
            } 
            catch (IOException ex)
            {
                Logger.error(this, "The server thread failed", ex);
            }
            catch (NoSuchModuleException ex)
            {
                Logger.error(this, "PANIC! Could not resolve server backend", ex);
            }
            finally
            {
                thread = null;
            }
        }

        public void start()
        {
            thread = new SshThread(this, "SSH Server", true);
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
                Logger.warn(this, 
                        "The listening socket reported an error during shutdown", ioe);
            }            
        }
    }

    public void start(Map<String, String> parameters)
    {
        this.parameters = parameters;
        // Setup J2SSH dummy configuration classes.
        //
        try
        {
            ConfigurationContext dummy = new DummyConfigurationContext(
                    parameters.get("serverhostkeyfile"), Integer
                            .parseInt(parameters.get("port")), Integer
                            .parseInt(parameters.get("maxauthretry")),
                    Integer.parseInt(parameters.get("maxconn")));
            ConfigurationLoader.initialize(true, dummy);
            selfRegister = "true".equals(parameters.get("selfRegister"));
        } catch (ConfigurationException e)
        {
            Logger.fatal(this,
                    "Cannot start SSH Server due to broken configuration.", e);
            return;
        }

        // Perform sanity-checks.
        //
        if (!this.sanityChecks())
        {
            Logger.fatal(this,
                    "FATAL: Cannot start SSH server due to the above errors.");
            return;
        }

        // Start server.
        //
        try
        {
            listener = new ConnectionListener(
                    ((ServerConfiguration) ConfigurationLoader
                            .getConfiguration(ServerConfiguration.class))
                            .getPort());
            listener.start();
        } catch (IOException e)
        {
            Logger.fatal(this, "Couldn't start SSH Server: " + e.getMessage(),
                    e);
            return;
        }

        Logger.info(this, "OpenKOM SSH server is accepting connections at port "
                        + (String)parameters.get("port"));

        try
        {
            lockoutTime = Long.parseLong(parameters.get("lockout"));
            lockoutTime *= 60000; // convert from minutes to milliseconds
        }
        catch (Exception e)
        {
            Logger.warn(this, "Lockout time parameter missing or unparsable, defaulting to 10 minutes.");
        }
        
        try
        {
            allowedAttempts = Integer.parseInt(parameters.get("attempts"));
        }
        catch (Exception e)
        {
            Logger.warn(this, "Allowed attempts parameter unparsable or missing from module parameters, defaulting to 3x3.");
        }
        
    }

    public void stop()
    {
        Logger.info(this, "Shutting down server");
        listener.stop();
    }

    public void join() throws InterruptedException
    {
        // There's nothing to wait for, so just return
    }

    protected boolean sanityChecks()
    {
        // Check if our dummy configuration classes are loaded properly.
        //
        boolean ok = true;
        try
        {
            if (!ConfigurationLoader
                    .isConfigurationAvailable(ServerConfiguration.class))
            {
                Logger.fatal(this, "Server configuration not avaialble!");
                ok = false;
            }

            if (!ConfigurationLoader
                    .isConfigurationAvailable(PlatformConfiguration.class))
            {
                Logger.fatal(this, "Platform configuration not avaialble!");
                ok = false;
            }

            if (((ServerConfiguration) ConfigurationLoader
                    .getConfiguration(ServerConfiguration.class))
                    .getServerHostKeys().size() <= 0)
            {
                Logger.fatal("Server cannot start because there are no server host keys available");
                ok = false;
            }
        } catch (ConfigurationException e)
        {
            Logger.fatal("Configuration error");
            ok = false;
        }

        return ok;
    }
}

