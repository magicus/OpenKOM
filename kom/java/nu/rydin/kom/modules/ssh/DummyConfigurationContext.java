/*
 * Created on Dec 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules.ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import nu.rydin.kom.utils.Logger;

import org.xml.sax.SAXException;

//import com.sshtools.common.configuration.SshAPIConfiguration;
import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.daemon.vfs.VFSMount;
import com.sshtools.j2ssh.configuration.ConfigurationContext;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;

/**
 * This class and its inner classes are only used as a work-around for the J2SSH
 * configuration framework. We do not want to provide the expected xml-files, we
 * just want programmatical control over the configuration and feed it values
 * from our own module configuration file.
 *  
 * @author Henrik Schröder
 */
class DummyConfigurationContext implements ConfigurationContext
{
    private HashMap<Class, Object> configurations = new HashMap<Class, Object>();

    private String serverHostKeyFile;

    private int port;

    private int maxAuthRetry;

    private int maxConn;

    public DummyConfigurationContext(String serverHostKeyFile, int port,
            int maxAuthRetry, int maxConn)
    {
        this.serverHostKeyFile = serverHostKeyFile;
        this.port = port;
        this.maxAuthRetry = maxAuthRetry;
        this.maxConn = maxConn;
    }

    public boolean isConfigurationAvailable(Class cls)
    {
        return true;
    }

    public Object getConfiguration(Class cls) throws ConfigurationException
    {
        if (configurations.containsKey(cls))
        {
            return configurations.get(cls);
        } else
        {
            throw new ConfigurationException(cls.getName()
                    + " configuration not available");
        }
    }

    public void initialize() throws ConfigurationException
    {
        try
        {
            ServerConfiguration fake1 = new DummyServerConfiguration(
                    serverHostKeyFile, port, maxAuthRetry, maxConn);
            configurations.put(ServerConfiguration.class, fake1);
            PlatformConfiguration fake2 = new DummyPlatformConfiguration();
            configurations.put(PlatformConfiguration.class, fake2);
//            SshAPIConfiguration fake3 = new DummySshAPIConfiguration();
//            configurations.put(SshAPIConfiguration.class, fake3);
        } catch (Exception ex)
        {
            Logger.fatal(this, ex);
            throw new ConfigurationException(ex.getMessage());
        }
    }

    private static class DummyPlatformConfiguration extends
            PlatformConfiguration
    {
        public DummyPlatformConfiguration() throws SAXException,
                ParserConfigurationException, IOException
        {
            super(null);
        }

        public void reload(InputStream in) throws SAXException,
                ParserConfigurationException, IOException
        {
            //Override to make sure noone does something stupid.
        }

        public String getNativeAuthenticationProvider()
        {
            return "nu.rydin.kom.modules.ssh.OpenKOMAuthenticationProvider";
        }

        public String getNativeFileSystemProvider()
        {
            return "";
        }

        public String getNativeProcessProvider()
        {
            return "";
        }

        public String getSetting(String name, String defaultValue)
        {
            return defaultValue;
        }

        public String getSetting(String name)
        {
            return "";
        }

        public Map getVFSMounts()
        {
            return null;
        }

        public VFSMount getVFSRoot()
        {
            return null;
        }
    }

    private static class DummyServerConfiguration extends ServerConfiguration
    {
        private Map<String, SshPrivateKey> serverHostKeys = new HashMap<String, SshPrivateKey>();

        private int port;

        private int maxAuthRetry;

        private int maxConn;

        public DummyServerConfiguration(String serverHostKeyFile, int port,
                int maxAuthRetry, int maxConn) throws SAXException,
                ParserConfigurationException, IOException
        {
            super(null);

            this.port = port;
            this.maxAuthRetry = maxAuthRetry;
            this.maxConn = maxConn;

            File f = new File(serverHostKeyFile);

            if (!f.exists())
            {
                serverHostKeyFile = ConfigurationLoader
                        .getConfigurationDirectory()
                        + serverHostKeyFile;
                f = new File(serverHostKeyFile);
            }

            try
            {
                if (f.exists())
                {
                    SshPrivateKeyFile pkf = SshPrivateKeyFile.parse(f);
                    SshPrivateKey key = pkf.toPrivateKey(null);
                    serverHostKeys.put(key.getAlgorithmName(), key);
                } else
                {
                    Logger.warn(this, "Private key file '" + serverHostKeyFile
                            + "' could not be found");
                }
            } catch (InvalidSshKeyException ex)
            {
                Logger.warn(this, "Failed to load private key '"
                        + serverHostKeyFile, ex);
            } catch (IOException ioe)
            {
                Logger.warn(this, "Failed to load private key '"
                        + serverHostKeyFile, ioe);
            }
        }

        public void reload(InputStream in) throws SAXException,
                ParserConfigurationException, IOException
        {
            //Override to make sure noone does something stupid.
        }

        public List<String> getAllowedAuthentications()
        {
            List<String> result = new ArrayList<String>();
            result.add("password");
            return result;
        }

        public boolean getAllowTcpForwarding()
        {
            return false;
        }

        public String getAuthenticationBanner()
        {
            //We could add a filename of a banner here later...
            return "";
        }

        public String getAuthorizationFile()
        {
            return "";
        }

        public int getCommandPort()
        {
            return 0;
        }

        public String getListenAddress()
        {
            return "";
        }

        public int getMaxAuthentications()
        {
            return maxAuthRetry;
        }

        public int getMaxConnections()
        {
            return maxConn;
        }

        public int getPort()
        {
            return port;
        }

        public List<String> getRequiredAuthentications()
        {
            return new ArrayList<String>();
        }

        public Map<String, SshPrivateKey> getServerHostKeys()
        {
            return serverHostKeys;
        }

        public Map getSubsystems()
        {
            return new HashMap();
        }

        public String getTerminalProvider()
        {
            //return "%DEFAULT_TERMINAL% /Q";
            return "";
        }

        public String getUserConfigDirectory()
        {
            //return "%D\\.ssh2";
            return "";
        }
    }

    /*
    private static class DummySshAPIConfiguration extends SshAPIConfiguration
    {
        public DummySshAPIConfiguration() throws SAXException,
                ParserConfigurationException, IOException
        {
            super(null);
        }

        public void reload(InputStream in) throws SAXException,
                ParserConfigurationException, IOException
        {
            //Override to make sure noone does something stupid.
        }

        public List getAuthenticationExtensions()
        {
            return new ArrayList();
        }

        public List getCipherExtensions()
        {
            return new ArrayList();
        }

        public List getCompressionExtensions()
        {
            return new ArrayList();
        }

        public String getDefaultCipher()
        {
            return "blowfish-cbc";
        }

        public String getDefaultCompression()
        {
            return "none";
        }

        public String getDefaultKeyExchange()
        {
            return "diffie-hellman-group1-sha1";
        }

        public String getDefaultMac()
        {
            return "hmac-md5";
        }

        public String getDefaultPrivateKeyFormat()
        {
            return "SSHTools-PrivateKey-Base64Encoded";
        }

        public String getDefaultPublicKey()
        {
            return "ssh-dss";
        }

        public String getDefaultPublicKeyFormat()
        {
            return "SECSH-PublicKey-Base64Encoded";
        }

        public List getKeyExchangeExtensions()
        {
            return new ArrayList();
        }

        public List getMacExtensions()
        {
            return new ArrayList();
        }

        public List getPrivateKeyFormats()
        {
            return new ArrayList();
        }

        public List getPublicKeyExtensions()
        {
            return new ArrayList();
        }

        public List getPublicKeyFormats()
        {
            return new ArrayList();
        }
    }
*/
}