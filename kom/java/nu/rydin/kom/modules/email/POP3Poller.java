package nu.rydin.kom.modules.email;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

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
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.utils.Logger;

public class POP3Poller extends Thread
{
    private final String host;

    private final int port;

    private final String user;

    private final String password;
    
    private final String postmaster;
    
    private final String postmasterPassword;
    
    private final String deadLetterArea;

    private final int pollDelay;
    
    private final long systemMessageConf;

    public POP3Poller(String host, int port, String user, String password,
            String postmaster, String postmasterPassword, int pollDelay,
            String deadLetterArea, long systemMessageConf)
    {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.postmaster = postmaster;
        this.postmasterPassword = postmasterPassword;
        this.pollDelay = pollDelay;
        this.deadLetterArea = deadLetterArea;
        this.systemMessageConf = systemMessageConf;
        this.setName("Postmaster");
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
                            ServerSession ss = null;
                            try
                            {
                                for (int idx = 0; idx < top; ++idx)
                                {
                                    Message each = messages[idx];
                                    Address[] fromAddresses = each.getFrom();
                                    if(fromAddresses == null || fromAddresses.length == 0)
                                    {
                                    	this.handleDeadLetter(each, "Null sender. Can't handle message", ss);
                                    	continue;
                                    }
                                    Address fromAddress = fromAddresses[0];
                                    if(fromAddress == null)
                                    {
                                    	this.handleDeadLetter(each, "Null sender. Can't handle message", ss);
                                    	continue;
                                    }
                                    if(!fromAddress.getType().equals("rfc822"))
                                    {
                                    	this.handleDeadLetter(each, "Can't handle address of type " + fromAddress.getType(), ss);
                                    	continue;
                                    }
                                    
                                    String from = ((InternetAddress) fromAddress).getAddress();
                                    
                                    // Strip the domain off the receiver
                                    //
                                    Address[] recipients = each.getRecipients(RecipientType.TO);
                                    if(recipients == null)
                                    {
                                    	this.handleDeadLetter(each, "Null recipients. Can't handle message from " + each.getFrom(), ss);
                                    	continue;
                                    }
                                    for(int idx2 = 0; idx2 < recipients.length; ++idx2)
                                    {
                                    	String to = ((InternetAddress) recipients[idx2]).getAddress();
                                    	Logger.info(this, "Processing email to " + to);
                                    	
                                    	// Looking good! Let's send it!
                                    	//
                                    	try
                                    	{
                                    		// Create server session of we don't already have one
                                    		//
                                    		if(ss == null)
                                    			ss = ssf.login(this.postmaster, this.postmasterPassword, ClientTypes.SOAP, true);
                                    		String subject = each.getSubject();
                                    		if(subject == null)
                                    			subject = "";
                                    		MessageOccurrence occ = ss.postIncomingEmail(from, to.substring(0, to.indexOf("@")), 
                                    				each.getSentDate(), each.getReceivedDate(), subject, this.getContent(each));
                                    		Logger.info(this, "Email from " + from + " accepted and stored as (" + occ.getGlobalId() + ")");
                                    		each.setFlag(Flags.Flag.DELETED, true);
                                    	}
                                    	catch(EmailRecipientNotRecognizedException e)
                                    	{
                                    		this.handleDeadLetter(each, "Recipient " + to + " not recoginized. Message skipped!", ss);
                                    		
                                    		// TODO: Maybe send something back?
                                    	}
                                    	catch(EmailSenderNotRecognizedException e)
                                    	{
                                    		this.handleDeadLetter(each, "Sender " + from + " not recoginized. Message skipped!", ss);
                                    	}
                                    	catch(AuthorizationException e)
                                    	{
                                    		this.handleDeadLetter(each, "Not authorized to store message. Check privileges of postmaster or that sender is member of destination conference!", ss);
                                    	}
                                        catch (UnexpectedException e)
                                        {
                                            Logger.error(this, "Internal error when processing email", e);
                                            this.handleDeadLetter(each, "Internal error, check logs!", ss);
                                        }
                                    }
                                }
                            }
                            finally
                            {
                            	if(ss != null)
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
    
    
    protected void handleDeadLetter(Message message, String reason, ServerSession ss) throws ObjectNotFoundException, AuthorizationException, UnexpectedException, IOException, MessagingException
    {
    	// Log warning
    	//
    	Logger.warn(this, reason);
    	
    	// Dump message to dead letter area
    	//
    	InputStream is = message.getInputStream();
    	PrintStream os = null;
    	File file;
    	for(;;)
    	{
	    	String filename = "dead_letter_" + UUID.randomUUID();
	    	file = new File(this.deadLetterArea, filename);
	    	
	    	// Check for the very unlikely condition that we already have
	    	// a file with the same name
	    	//
	    	if(!file.exists())
	    	{
	    		// Didn't exist. We're good!
	    		//
	    		os = new PrintStream(new FileOutputStream(file));
	    		break;
	    	}
    	}
    	
    	// Copy entire email to dead letter file
    	//
    	// First, dump all headers.
    	// TODO: There HAS to be a way to stream out the entire raw message using JavaMail!!!
    	//
    	for(Enumeration en = message.getAllHeaders(); en.hasMoreElements();)
    	{
    		Header header = (Header) en.nextElement();
    		os.print(header.getName());
    		os.print(": ");
    		os.println(header.getValue());
    	}
    	os.println();
    	byte[] buffer = new byte[10000];
    	int n;
    	while((n = is.read(buffer)) > 0)
    		os.write(buffer, 0, n);
    	os.close();
    	
    	// Notify operators by posting a message in the system message
    	// conference
    	//
    	StringBuffer msg = new StringBuffer(5000); 
    	msg.append("Undeliverable incoming email message.\n\n");
    	msg.append("Reason: ");
    	msg.append(reason);
    	msg.append("\n\nFor the entire message, please refer to local file ");
    	msg.append(file.getAbsolutePath());
    	msg.append(". \n\nHeaders follow:\n\n");
    	for(Enumeration en = message.getAllHeaders(); en.hasMoreElements();)
    	{
    		Header header = (Header) en.nextElement();
    		msg.append(header.getName());
    		msg.append(": ");
    		msg.append(header.getValue());
    		msg.append('\n');
    	}
    	UnstoredMessage notification = new UnstoredMessage("Undeliverable email", msg.toString());
    	ss.storeMessage(this.systemMessageConf, notification);
    	
    	// Getting here means all is well, so we can delete the message!
    	//
    	message.setFlag(Flags.Flag.DELETED, true);
    }

    protected String getContent(Part message) throws MessagingException, IOException
    {
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
            return "<Warning: Empty message or no plain-text part>";
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
            return "<Warning: Didn't find any part of the message I could understand (probably all HTML without plain-text part)>";
        }
    }
}
