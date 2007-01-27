package nu.rydin.kom.modules.email;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import com.sun.mail.pop3.POP3Store;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.constants.ClientTypes;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.EmailRecipientNotRecognizedException;
import nu.rydin.kom.exceptions.EmailSenderNotRecognizedException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.utils.Logger;

public class POP3Poller extends Thread
{
    private final String host;

    private final int port;

    private final String user;

    private final String password;

    private final String domain;
    
    private final String postmaster;
    
    private final String postmasterPassword;

    private final int pollDelay;

    public POP3Poller(String host, int port, String user, String password,
            String domain, String postmaster, String postmasterPassword, int pollDelay)
    {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.domain = domain;
        this.postmaster = postmaster;
        this.postmasterPassword = postmasterPassword;
        this.pollDelay = pollDelay;
    }

    public void run()
    {
        Properties props = System.getProperties();

        // Get a Session object
        //
        Session session = Session.getInstance(props, null);
        session.setDebug(false);
        
        // Get an OpenKOM session factory
        //
        ServerSessionFactory ssf = null;
        try
        {
            ssf = (ServerSessionFactory) Modules.getModule("Backend");
        }
        catch(NoSuchModuleException e)
        {
            Logger.fatal(this, "Backend not running! Can't start postmaster");
            return;
        }
        Logger.info(this, "Polling mailbox " + user + " at host " + host);
        try
        {
        	int dl = this.domain.length();
            for (;;)
            {
                boolean commit = false;
                try
                {	
                    Store store = session.getStore("pop3");
                    store.connect(host, port, user, password);
                    Folder folder = store.getFolder("INBOX");
                    folder.open(Folder.READ_WRITE);
                    try
                    {
                        Message[] messages = folder.getMessages();
                        int top = messages.length;
                        if(top > 0)
                        {
                            // There are messages to process! Get us a server session! 
                            //
                            ServerSession ss = ssf.login(this.postmaster, this.postmasterPassword, ClientTypes.SOAP, true);
                            try
                            {
                                for (int idx = 0; idx < top; ++idx)
                                {
                                    Message each = messages[idx];
                                    Address[] fromAddresses = each.getFrom();
                                    if(fromAddresses == null || fromAddresses.length == 0)
                                    {
                                    	Logger.warn(this, "Null sender. Can't handle message");
                                    	each.setFlag(Flags.Flag.DELETED, true);
                                    	continue;
                                    }
                                    Address fromAddress = fromAddresses[0];
                                    if(fromAddress == null)
                                    {
                                    	Logger.warn(this, "Null sender. Can't handle message");
                                    	each.setFlag(Flags.Flag.DELETED, true);
                                    	continue;
                                    }
                                    if(!fromAddress.getType().equals("rfc822"))
                                    {
                                    	Logger.warn(this, "Can't handle address of type " + fromAddress.getType());
                                    	each.setFlag(Flags.Flag.DELETED, true);
                                    	continue;
                                    }
                                    
                                    String from = ((InternetAddress) fromAddress).getAddress();
                                    
                                    // Strip the domain off the receiver
                                    //
                                    Address[] recipients = each.getRecipients(RecipientType.TO);
                                    if(recipients == null)
                                    {
                                    	Logger.warn(this, "Null recipients. Can't handle message from " + each.getFrom());
                                    	continue;
                                    }
                                    for(int idx2 = 0; idx2 < recipients.length; ++idx2)
                                    {
                                    	String to = ((InternetAddress) recipients[idx2]).getAddress();
                                    	
                                    	// We deal only with emails sent to "our" domain
                                    	//
                                    	Logger.info(this, "Processing " + to);
                                    	if(!to.endsWith(this.domain))
                                    		continue;
                                    	
                                    	// Looking good! Let's send it!
                                    	//
                                    	try
                                    	{
                                    		MessageOccurrence occ = ss.postIncomingEmail(from, to.substring(0, to.length() - dl - 1), 
                                    				each.getSubject(), this.getContent(each));
                                    		Logger.info(this, "Email from " + from + " accepted and stored as (" + occ.getGlobalId() + ")");
                                    		each.setFlag(Flags.Flag.DELETED, true);
                                    	}
                                    	catch(EmailRecipientNotRecognizedException e)
                                    	{
                                    		Logger.warn(this, "Recipient " + to + " not recoginized. Message skipped!");
                                    		each.setFlag(Flags.Flag.DELETED, true);
                                    		
                                    		// TODO: Maybe send something back?
                                    	}
                                    	catch(EmailSenderNotRecognizedException e)
                                    	{
                                    		Logger.warn(this, "Sender " + from + " not recoginized. Message skipped!");
                                    		each.setFlag(Flags.Flag.DELETED, true);
                                    	}
                                    	catch(AuthorizationException e)
                                    	{
                                    		Logger.warn(this, "Not authorized to store message. Check privileges of postmaster!");
                       
                                    		// Don't delete!
                                    	}
                                    }
                                }
                            }
                            finally
                            {
                                ss.close();
                            }
                        }
                        // We made it through, so we can commit changes!
                        //
                        commit = true;
                    }
                    catch(LoginProhibitedException e)
                    {
                        Logger.error(this, "Login prohibited. Trying again...");
                    } 
                    catch (AuthenticationException e)
                    {
                        Logger.error(this, "Cannot log in postmaster. Wrong user/password. Trying again...");
                    } 
                    catch (AlreadyLoggedInException e)
                    {
                        Logger.error(this, "Congratulations! This can't happen!");
                    } 
                    catch (UnexpectedException e)
                    {
                        Logger.error(this, "Expect the unexpected!", e);
                    }
                    finally
                    {
                        folder.close(commit);
                        store.close();
                    }
                } 
                catch (MessagingException e)
                {
                    Logger.error(this, "Error fetching email", e);
                } 
                catch (IOException e)
                {
                    Logger.error(this, "Error fetching email", e);
                }
                catch(Exception e)
                {
                	Logger.error(this, "Uncaught exception", e);
                }
                Thread.sleep(pollDelay);
            }
        } catch (InterruptedException e)
        {
            Logger.info(this, "Shutting down");
        }
    }

    public String getContent(Part message) throws MessagingException, IOException
    {
        String ct = message.getContentType();
        
        // Act according to mime type
        //
        if (message.isMimeType("text/plain"))
            return (String) message.getContent();
        else if (message.isMimeType("multipart/*"))
        {
            Multipart mp = (Multipart) message.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
            {
                String content = this.getContent(mp.getBodyPart(i));
                if(content != null)
                    return content;
            }
            
            // Getting here means we didn't find anything we understand
            // 
            return null;
        } 
        else if (message.isMimeType("message/rfc822"))
        {
            // Nested message
            //
            return this.getContent((Part) message.getContent());
        } 
        else
        {
            // No comprende...
            //
            return null;
        }
    }
}
