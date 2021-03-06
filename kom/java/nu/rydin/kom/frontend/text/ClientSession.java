/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.ServerSessionFactory;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.constants.ClientTypes;
import nu.rydin.kom.constants.CommandSuggestions;
import nu.rydin.kom.constants.MessageLogKinds;
import nu.rydin.kom.constants.SystemFiles;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.events.BroadcastAnonymousMessageEvent;
import nu.rydin.kom.events.BroadcastMessageEvent;
import nu.rydin.kom.events.ChatAnonymousMessageEvent;
import nu.rydin.kom.events.ChatMessageEvent;
import nu.rydin.kom.events.ClientEventTarget;
import nu.rydin.kom.events.DetachRequestEvent;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.MessageDeletedEvent;
import nu.rydin.kom.events.NewMessageEvent;
import nu.rydin.kom.events.ReloadUserProfileEvent;
import nu.rydin.kom.events.TicketDeliveredEvent;
import nu.rydin.kom.events.UserAttendanceEvent;
import nu.rydin.kom.exceptions.AlreadyLoggedInException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthenticationException;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.EventDeliveredException;
import nu.rydin.kom.exceptions.ImmediateShutdownException;
import nu.rydin.kom.exceptions.InternalException;
import nu.rydin.kom.exceptions.InvalidChoiceException;
import nu.rydin.kom.exceptions.InvalidNameException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.KOMRuntimeException;
import nu.rydin.kom.exceptions.LineEditingDoneException;
import nu.rydin.kom.exceptions.LineEditorException;
import nu.rydin.kom.exceptions.LineOverflowException;
import nu.rydin.kom.exceptions.LineUnderflowException;
import nu.rydin.kom.exceptions.LoginNotAllowedException;
import nu.rydin.kom.exceptions.LoginProhibitedException;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.OutputInterruptedException;
import nu.rydin.kom.exceptions.StopCharException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.exceptions.UserException;
import nu.rydin.kom.frontend.text.commands.GotoNextConference;
import nu.rydin.kom.frontend.text.commands.ReadNextMail;
import nu.rydin.kom.frontend.text.commands.ReadNextMessage;
import nu.rydin.kom.frontend.text.commands.ReadNextReply;
import nu.rydin.kom.frontend.text.commands.ReadNextSelectedMessage;
import nu.rydin.kom.frontend.text.commands.ShowTime;
import nu.rydin.kom.frontend.text.editor.StandardWordWrapper;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.editor.WordWrapperFactory;
import nu.rydin.kom.frontend.text.editor.fullscreen.FullscreenMessageEditor;
import nu.rydin.kom.frontend.text.editor.simple.SimpleMessageEditor;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.frontend.text.parser.Parser.ExecutableCommand;
import nu.rydin.kom.frontend.text.terminal.ANSITerminalController;
import nu.rydin.kom.frontend.text.terminal.TerminalController;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.FileStatus;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.SessionListItem;
import nu.rydin.kom.structs.SessionState;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.utils.FileUtils;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.Logger;
import nu.rydin.kom.utils.PrintUtils;
import nu.rydin.kom.utils.StringUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 * @author Henrik Schr�der
 * @author Magnus Ihse Bursie
 */
public class ClientSession implements Runnable, Context, ClientEventTarget, TerminalSizeListener, EnvironmentListener
{
	private static final int MAX_LOGIN_RETRIES = 3;
	private static final String DEFAULT_CHARSET = "ISO-8859-1";
	
	private LineEditor m_in;
	private KOMWriter m_out;
	private final InputStream m_rawIn;
	private final OutputStream m_rawOut; 
	private MessageFormatter m_formatter = new MessageFormatter(Locale.getDefault(), "messages");
	private ServerSession m_session;
	private long m_userId;
	private LinkedList<Event> m_displayMessageQueue = new LinkedList<Event>();
	private UserInfo m_thisUserCache;
	private WordWrapperFactory m_wordWrapperFactory = new StandardWordWrapper.Factory();
	private int m_windowHeight = -1;
	private int m_windowWidth = -1;
	private boolean m_ListenToTerminalSize = true;
	private EventPrinter eventPrinter;
	private Locale m_locale;
	private DateFormatSymbols m_dateSymbols;
	private final boolean m_useTicket;
	private SessionState m_state;
	private String m_ticket;
	private String clientName;
	private int retriesBeforeLockout = 9;
	private long lockout = 900000;
	private long failedLoginDelay = 3000;

	private Parser m_parser;
    private boolean m_loggedIn;	
    
    private class HeartbeatSender extends Thread implements KeystrokeListener
    {
        private boolean m_idle = false;
        
        public void keystroke(char ch)
        {
            // Are we idle? Send heartbeat immediately!
            //
            if(m_idle)
            {
                synchronized(this)
                {
                    this.notify();
                }
                m_idle = false;
            }
        }
        public void run()
        {
            
            for(;;)
            {
                // Sleep for 30 seconds
                //
                try
                {
                    synchronized(this)
                    {
                        this.wait(30000);
                    }
                }
                catch(InterruptedException e)
                {
                    break;
                }
                
                // Any activity the last 30 seconds? Send hearbeat!
                //
                if(System.currentTimeMillis() - ClientSession.this.m_in.getLastKeystrokeTime() < 30000)
                    ClientSession.this.getSession().getHeartbeatListener().heartbeat();
                else
                    m_idle = true;
            }
        }
    }
    
    private HeartbeatSender m_heartbeatSender ;
    
	private class EventPrinter implements EventTarget
	{
		private Context context;
		
		public EventPrinter (Context context)
		{
			this.context = context;
		}
		
		private void printWrapped(String message, int offset)
		{
		    WordWrapper ww = getWordWrapper(message, getTerminalSettings().getWidth(), offset);
		    String line = null;
		    while((line = ww.nextLine()) != null)
		    {
		    	m_out.println(line);
		    }
		}
		
		public void onEvent(Event event) 
		{
		    // Unknown event. Not much we can do.
		}
	
		public void onEvent(ChatMessageEvent event) 
		{
			this.beepMaybe(UserFlags.BEEP_ON_CHAT);
			String header = m_formatter.format("event.chat", 
                    new Object[] { formatObjectName(event.getUserName(), event.getOriginatingUser()) }); 
			getDisplayController().chatMessageHeader();
			m_out.print(header);
			getDisplayController().chatMessageBody();
			printWrapped(event.getMessage(), header.length());
			m_out.println();
		}

		public void onEvent(BroadcastMessageEvent event) 
		{
		    try
		    {
    		    boolean more = (context.getCachedUserInfo().getFlags1() & UserFlags.MORE_PROMPT_IN_BROADCAST) != 0;
    		    context.getIn().setPageBreak(more);
    			this.beepMaybe(UserFlags.BEEP_ON_BROADCAST);
                String name = formatObjectName(event.getUserName(), event.getOriginatingUser());
    			String header = event.getKind() == MessageLogKinds.BROADCAST 
    			    ? m_formatter.format("event.broadcast.default", 
                            new Object[] { name }) 
    			    : name + ' ';
    			getDisplayController().broadcastMessageHeader();
    			m_out.print(header);
    			if (event.getKind() == MessageLogKinds.BROADCAST)
    			{
    			    getDisplayController().broadcastMessageBody();
    			}
    			printWrapped(event.getMessage(), header.length());
    			m_out.println();
    			context.getIn().setPageBreak(true);
		    }
		    catch(UnexpectedException e)
		    {
		        Logger.error(this, "Error in broadcast message handler", e);
		    }
		}

		public void onEvent(ChatAnonymousMessageEvent event) 
		{
			this.beepMaybe(UserFlags.BEEP_ON_CHAT);
			String header = m_formatter.format("event.chat.anonymous");
			getDisplayController().chatMessageHeader();
			m_out.print(header);
			getDisplayController().chatMessageBody();
			printWrapped(event.getMessage(), header.length());
			m_out.println();			
		}

		public void onEvent(BroadcastAnonymousMessageEvent event) 
		{
			this.beepMaybe(UserFlags.BEEP_ON_BROADCAST);
			String header = m_formatter.format("event.broadcast.anonymous");
			getDisplayController().broadcastMessageHeader();
			m_out.print(header);
			getDisplayController().broadcastMessageBody();
			printWrapped(event.getMessage(), header.length());
			m_out.println();			
		}

		public void onEvent(NewMessageEvent event) 
		{
			//This event should not be handled here
		}

		public void onEvent(UserAttendanceEvent event) 
		{
			getDisplayController().broadcastMessageHeader();
			this.beepMaybe(UserFlags.BEEP_ON_ATTENDANCE);			
			m_out.println(m_formatter.format("event.attendance." + event.getType(), new Object[] { context.formatObjectName(event.getUserName(), event.getOriginatingUser()) } ));
			m_out.println();
		}

		public void onEvent(ReloadUserProfileEvent event) 
		{
			//This event should not be handled here
		}

		public void onEvent(MessageDeletedEvent event) 
		{
			//This event should not be handled here
		}
		
		protected void beepMaybe(long flag)
		{
			try
			{
			    // Beep if user wants it
			    //
				if((ClientSession.this.getCachedUserInfo().getFlags1() & flag) != 0)
				    m_out.print('\u0007');
			}
			catch(UnexpectedException e)
			{
			    // Should NOT happen!
			    //
			    throw new RuntimeException(e);
			}					    
		}
	}
	
	public ClientSession(InputStream in, OutputStream out, boolean useTicket, boolean selfRegister, String clientName, Map<String, String> parameters)
	throws UnexpectedException, InternalException
	{
	    // Unpack properties
	    //
	    
	   try
	   {
	       retriesBeforeLockout = Integer.parseInt(parameters.get("attempts"));
	   }
	   catch(NullPointerException e) {  } // Just keep default
	   catch(NumberFormatException e) 
	   {  
	       Logger.warn(this, "Error parsing 'attempts' parameter, using default");
	   }
	   
       try
       {
           lockout = Long.parseLong(parameters.get("lockout"));
       }
       catch(NullPointerException e) {  } // Just keep default
       catch(NumberFormatException e) 
       {  
           Logger.warn(this, "Error parsing 'lockout' parameter, using default");
       }


		// Set up I/O
		//
		m_rawIn = in;
		m_rawOut = out;
		this.clientName = clientName;
		eventPrinter = new EventPrinter (this);
		
		// Set up authentication method
		//
		m_useTicket = useTicket;
						
		// Install commands and init parser
		//
		this.installCommands();
		
		// More I/O
		//
		try
		{
			m_out = new KOMWriter(m_rawOut, DEFAULT_CHARSET);
			m_in = new LineEditor(m_rawIn, m_out, this, this, null, m_formatter, DEFAULT_CHARSET);
			m_out.addNewlineListener(m_in);
		}
		catch(UnsupportedEncodingException e)
		{
			// There're NO WAY we don't support US-ASCII!
			//
			throw new InternalException(DEFAULT_CHARSET + " not supported. Your JVM is broken!");
		}		
	}
	 
	public void run()
	{ 
		try
		{
		    // Start keystroke poller
		    //
		    m_in.start();
		    
            // Print welcome message if defined
            //
		    if(!m_useTicket)
		    {
	            try
	            {
	                String content = FileUtils.loadTextFromResource("welcome.txt");
	                PrintUtils.printIndented(m_out, content, 80, 0);
	                m_out.println();
	            }
	            catch(FileNotFoundException e)
	            {
	                // No message defined, no problem.
	            }
	            catch(IOException e)
	            {
	                m_out.println("This should not happen!");
	            }
		    }

			// Try to login
			//
			UserInfo userInfo = null;
			try
			{
				for(int idx = 0; idx < MAX_LOGIN_RETRIES; ++idx)
				{
					try
					{
						userInfo = this.login();
						m_userId = userInfo.getId();
						Thread.currentThread().setName("Session (" + userInfo.getUserid() + ")");
						break;
					}
					catch(AuthenticationException e)
					{
					    // Logging in with ticket? This HAS to work
					    //
					    if(m_useTicket)
					        return;
						m_out.println(m_formatter.format("login.failure"));
						Logger.info(this, "Failed login");
					}
				}
			}
			catch(LoginProhibitedException e)
			{
    		    m_out.println();
    			m_out.println(e.formatMessage(this));
    			m_out.println();
    			return;
			}
			catch(LoginNotAllowedException e)
			{
    		    m_out.println();
    			m_out.println(e.formatMessage(this));
    			m_out.println();
    			return;
			}
			catch(InterruptedException e)
			{
				// Interrupted during login
				//
				return;
			}
			catch(OperationInterruptedException e)
			{
				// Interrupted during login
				//
				return;
			}
			catch(Exception e)
			{
				// Unhandled exception while logging in? 
				// I'm afraid the ride is over...
				//
			    Logger.warn(this, "Unhandled exception during login?", e);
				return;
			}
			
			// Didn't manage to log in? Game over!
			//
			if(userInfo == null)
				return;		
			
			// Set up IO with the correct character set
			//
			String charSet = userInfo.getCharset();
			for(;;)
			{
				try
				{
					m_out.setCharset(charSet);
					m_in.setCharset(charSet);
					break;
				}
				catch(UnsupportedEncodingException e)
				{
					// Doesn't even support plain US-ASCII? We're outta here
					//
					if(charSet.equals("US-ASCII"))
						throw new RuntimeException("No suitable character set!");	
						
					// Resort to US-ASCII
					//
					charSet = "US-ASCII";
				}
			}
			
			// Replace message formatter with a localized one
			//
			String locale = userInfo.getLocale();
			if(locale != null)
			{
			    int p = locale.indexOf('_');
			    m_locale = p != -1 
			    	? new Locale(locale.substring(0, p), locale.substring(p + 1))
			    	: new Locale(locale);
			    m_formatter = new MessageFormatter(m_locale, "messages");
			}
			else
			    m_locale = Locale.getDefault();
			m_dateSymbols = new DateFormatSymbols(m_locale);
			m_formatter.setTimeZone(userInfo.getTimeZone());
				
			m_out.println(m_formatter.format("login.welcome", userInfo.getName()));
			m_out.println();
				
			userInfo = null; // Don't need it anymore... Let it be GC'd
			
			// Print motd (if any)
			//
			try
			{
			    String motd = m_session.readSystemFile(SystemFiles.WELCOME_MESSAGE);
			    this.getDisplayController().output();
			    m_out.println();
			    WordWrapper ww = this.getWordWrapper(motd);
			    String line;
			    while((line = ww.nextLine()) != null)
			        m_out.println(line);
			    m_out.println();
			}
			catch(ObjectNotFoundException e)
			{
			    // No motd. No big deal.
			}
			catch(AuthorizationException e)
			{
			    Logger.error(this, "Users don't have permission for motd");
			}
			catch(UnexpectedException e)
			{
			    Logger.error(this, e);
			}
			
			// Run the login and profile script
			//
			this.getDisplayController().normal();
			try
			{
				// Get profile scripts
				//
				FileStatus[] profiles = m_session.listFiles(this.getLoggedInUserId(), ".profile.%.cmd");
				int nProfiles = profiles.length;
				String profile = null;
				if(nProfiles == 1)
				    profile = profiles[0].getName();
				else if(nProfiles > 1)
				{
				    // More than one profile. Ask user.
				    //
				    for(;;)
				    {
					    m_out.println(m_formatter.format("login.profiles"));
					    for(int idx = 0; idx < nProfiles; ++idx)
					    {
					        String name = profiles[idx].getName();
					        m_out.print(idx + 1);
					        m_out.print(". ");
					        m_out.println(name.substring(9, name.length() - 4));
					    }
					    m_out.println();
					    m_out.print(m_formatter.format("login.choose.profile"));
					    try
					    {
					        String choiceStr = m_in.innerReadLine("1", "", 3, 0);
					        try
					        {
					            int choice = Integer.parseInt(choiceStr);
					            profile = profiles[choice - 1].getName();
					            break;
					        }
					        catch(NumberFormatException e)
					        {
					            // Bad choice 
					            //
					            m_out.println();
					            m_out.println(m_formatter.format("login.profil.invalid.choice"));
					        }
					        catch(ArrayIndexOutOfBoundsException e)
					        {
					            // Bad choice 
					            //
					            m_out.println();
					            m_out.println(m_formatter.format("login.profil.invalid.choice"));
					        }					        
					    }
					    catch(EventDeliveredException e)
					    {
					        // Should not happen
					        //
					        throw new UnexpectedException(this.getLoggedInUserId(), e);
					    }
				    }
				}
				
				String loginScript = null;
				String profileScript = null;
				try
				{
				    loginScript = m_session.readFile(this.getLoggedInUserId(), ".login.cmd");
				    if(profile != null)
				        profileScript = m_session.readFile(this.getLoggedInUserId(), profile);
				}
				catch(ObjectNotFoundException e)
				{
				    // No login script. Not much to do
				    //
				}
				try
				{
					if(profileScript != null)
					    this.executeScript(profileScript);
					if(loginScript != null)
					    this.executeScript(loginScript);
				}
				catch(OutputInterruptedException e)
				{
				    m_out.println(e.formatMessage(this));
				    m_out.println();
				}				
				catch(OperationInterruptedException e)
				{
				    m_out.println(e.formatMessage(this));
				    m_out.println();
				}
			}
			catch(KOMException e)
			{
			    m_out.println(e.formatMessage(this));
			    m_out.println();
			}
			catch(IOException e)
			{
			    e.printStackTrace(m_out);
			}
			catch(InterruptedException e)
			{
			    return;
			}
			catch(ImmediateShutdownException e)
			{
			    return;
			}
			
			// Start heartbeat sender
			//
			m_heartbeatSender = new HeartbeatSender();
			m_heartbeatSender.setName("Heartbeat sender (" + m_thisUserCache.getUserid() + ')');
			m_heartbeatSender.start();
			m_in.setKeystrokeListener(m_heartbeatSender);
			
			// Ensure that more prompts will be shown (if the user choose goAhead during login).
			this.getIn().setPageBreak(true);
			
			// MAIN SCREEN TURN ON!
			//		
			this.mainloop();
			m_out.println();
			m_out.println();
			try
			{
			    m_out.println(m_formatter.format("login.goodbye", this.getCachedUserInfo().getName()));
			}
			catch(UnexpectedException e)
			{
			    // Probably issues getting hold of user. Just get us out of here!
			    //
			    Logger.error(this, "Error logging out", e);
			}
		}
		finally
		{
			// Shut down...
			//
			try
			{
				// Clean up...
				//
				if(m_session != null)
				{
					synchronized(this)
					{
						m_session.close();
						m_session = null;
					}
				}
			}
			catch(Exception e)
			{
				// Ooops! Exception while cleaning up! 
				//
				Logger.error(this, m_formatter.format("logout.failure"), e);
			}
		}
	}

	protected UserInfo login()
	throws AuthenticationException, LoginNotAllowedException, UnexpectedException, IOException, 
	InterruptedException, OperationInterruptedException, LoginProhibitedException, NoSuchModuleException,
	AuthorizationException
	{
	    // Find backend. We are going to need it
	    //
	    ServerSessionFactory ssf = (ServerSessionFactory) Modules.getModule("Backend");
	    boolean selfRegister = ssf.allowsSelfRegistration();
	    
		// Collect information
		//
	    String ticket = null;
	    String password = null;
	    String userid = null;
	    if(m_useTicket)
	        ticket = this.waitForTicket();
	    else
	    {
	        try
	        {
	            // Print self-registration prompt if requested
	            //
	            String srToken = m_formatter.format("login.self.register.token");
	            if(selfRegister)
	            {
	                
	                m_out.println(m_formatter.format("login.self.register", srToken));
	                m_out.println();
	            }
				m_out.print(m_formatter.format("login.user"));
				m_out.flush();
				userid = m_in.readLine(null, null, 100, LineEditor.FLAG_ECHO);
				
				// Self registration?
				//
				if(selfRegister && srToken.equals(userid))
				{
				    String[] login = this.selfRegister();
				    userid = login[0];
				    password = login[1];
				}
				else
				{
				    // Normal login
				    //
					Logger.info(this, "Trying to login as: " + userid);
					m_out.print(m_formatter.format("login.password"));
					m_out.flush();
					password = m_in.readLine(null, null, 100, 0);
				}
	        }
	        catch(LineEditorException e)
	        {
	            throw new RuntimeException("This should not happen!", e);
	        }
	    }
		
		// Authenticate
		//
		try
		{
			m_session = m_useTicket
                ? ssf.login(ticket, ClientTypes.TTY, false) 
                : ssf.login(userid, password, ClientTypes.TTY, false);
            ssf.notifySuccessfulLogin(clientName);
		}
        catch(AuthenticationException e)
        {
            // For ticket-based logins, this will be handles elsewhere
            //
            if(!m_useTicket)
            {
                ssf.notifyFailedLogin(clientName, retriesBeforeLockout, lockout);
                
                // Sleep for a while to make brute force attacks a bit harder...
                //
                Thread.sleep(failedLoginDelay);
                
                // Rethrow, we will handle this in the mainloop of the shell
                //
                throw e;
            }
        }
		catch(AlreadyLoggedInException e)
		{
			// Already logged in. Ask if they want to create another session
			//
			m_out.println(m_formatter.format("login.multiple.session"));
            m_out.println();

            // Print headers
            //
            HeaderPrinter hp = new HeaderPrinter();
            hp.addHeader(m_formatter.format("list.sessions.session"), 7, true);
            hp.addHeader(m_formatter.format("list.sessions.login"), 7, true);
            hp.addHeader(m_formatter.format("list.sessions.idle"), 7, true);
            hp.addHeader(m_formatter.format("list.sessions.client"), 7, true);
            hp.addSpace(1);
            //int termWidth = this.getTerminalSettings().getWidth();
            //int firstColsWidth = 7 + 7 + 7 + 7 + 1;
            //int lastColWidth = termWidth - firstColsWidth - 1 ; 
            hp.printOn(m_out);
            
            // Print list
            List<SessionListItem> list = e.getSessions();
            for(Iterator<SessionListItem> itor = list.iterator(); itor.hasNext();)
            {
                SessionListItem each = itor.next();
                PrintUtils.printRightJustified(m_out, Integer.toString(each.getSessionId()), 7);
                long now = System.currentTimeMillis();
                PrintUtils.printRightJustified(m_out, StringUtils.formatElapsedTime(now - each.getLoginTime()), 7);
                long idle = now - each.getLastHeartbeat();
                PrintUtils.printRightJustified(m_out, idle >= 60000 ? StringUtils.formatElapsedTime(now - each.getLastHeartbeat()) : "", 7);
                m_out.print(' ');
                PrintUtils.printLeftJustified(m_out, m_formatter.format("clienttypes." + Integer.toString(each.getSessionType())), 7);
                m_out.println();
            }

            m_out.println();
            m_out.println(m_formatter.format("login.multiple.session.question.1"));
            m_out.println(m_formatter.format("login.multiple.session.question.2"));
            m_out.println(m_formatter.format("login.multiple.session.question.3"));
            m_out.println();
			m_out.flush();
			try
			{
                int choice = m_in.getChoice(m_formatter.format("login.multiple.session.question"), new String[] { "1", "2", "3"}, -1, m_formatter.format("misc.invalid.choice"));
                switch(choice)
                {
                case 0:
                    // Kill this session
                    //
                    throw new InterruptedException();
                case 2:
                    // Kill all other sessions
                    //
                    for(Iterator<SessionListItem> itor = list.iterator(); itor.hasNext();)
                    {
                        if(m_useTicket)
                            ssf.killSession(itor.next().getSessionId(), m_ticket);
                        else
                            ssf.killSession(itor.next().getSessionId(), userid, password);
                    }
                    
                    // FALL THRU
                case 1: 
                    // Create new session
                    //
                    try
                    {
                        m_session = m_useTicket
                            ? ssf.login(ticket, ClientTypes.TTY, true) 
                            : ssf.login(userid, password, ClientTypes.TTY, true);
                    }
                    catch(AlreadyLoggedInException e1)
                    {
                        // Huh? We DID allow this!
                        //
                        Logger.error(this, "This cannot happen!", e1);
                    }
                    Logger.info(this, "Allowed multiple login.");
                }			
			}
			catch(LineEditingDoneException e1)
			{
			    throw new RuntimeException("This should not happen", e1);
			}

		}
		// User was authenticated! Now check if they are allowed to log in.
		//
		UserInfo user = m_session.getLoggedInUser();
		if(!user.hasRights(UserPermissions.LOGIN))
			throw new LoginNotAllowedException();
			
		// Everything seems fine! We're in!
		//
		m_in.setSession(m_session);
		m_loggedIn = true;		
		Logger.info(this, "Successfully logged in as: " + m_session.getLoggedInUser().getUserid());
		return user;
	}
	
	public String[] selfRegister() 
	throws LineOverflowException, StopCharException, LineUnderflowException, 
	EventDeliveredException, LineEditingDoneException, OperationInterruptedException, 
	IOException, InterruptedException, NoSuchModuleException, 
	AuthorizationException, UnexpectedException
	{
	    // Find backend. We are going to need it
	    //
	    ServerSessionFactory ssf = (ServerSessionFactory) Modules.getModule("Backend");
	    
	    // Ask for character set
	    //
	    String charSet = "ISO-8859-1";
        ArrayList<String> list = new ArrayList<String>();
        for(StringTokenizer st = new StringTokenizer(ClientSettings.getCharsets(), ",");
        	st.hasMoreTokens();)
        	list.add(st.nextToken());        	
        for(;;)
        {
            m_out.println();
            try
            {
	        	int n = Parser.askForResolution(this, list, "parser.parameter.charset.ask", true, 
	        	        "parser.parameter.charset.ask", false, "charset");
	        	charSet = (String) list.get(n);
	        	m_in.setCharset(charSet);
	        	m_out.setCharset(charSet);
	        	break;
            }
            catch(InvalidChoiceException e)
            {
                // Try again
            }
        }
        
	    for(;;)
	    {
		    String password;
		    String userid;
		    String fullname;
		    for(;;)
		    {
			    m_out.print(m_formatter.format("login.self.register.login"));
			    m_out.flush();
			    userid = m_in.readLine(null, null, 100, LineEditor.FLAG_ECHO);
			    if(userid.length() == 0)
			        continue;
			    if(!ssf.loginExits(userid))
			        break;
			    m_out.println(m_formatter.format("login.self.register.duplicate"));
			    m_out.println();		    
		    }
		    for(;;)
		    {
			    m_out.print(m_formatter.format("login.self.register.password"));
			    m_out.flush();	    
			    password = m_in.readLine(null, null, 100, 0);
			    if(password.length() == 0)
			        continue;
			    m_out.print(m_formatter.format("login.self.register.password.verify"));
			    m_out.flush();
			    String verify = m_in.readLine(null, null, 100, 0);
			    if(password.equals(verify))
			        break;
			    m_out.println(m_formatter.format("login.self.register.password.mismatch"));
			    m_out.println();		    
		    }
		    for(;;)
		    {
			    m_out.print(m_formatter.format("login.self.register.fullname"));
			    m_out.flush();	    
			    fullname = m_in.readLine(null, null, 100, LineEditor.FLAG_ECHO);
			    if(fullname.length() > 0)
			        break;
		    }
		    
		    // Register user
		    //
		    try
		    {
			    ssf.selfRegister(userid, password, fullname, charSet);
			    m_out.println();
			    return new String[] { userid, password, fullname };
		    }
		    catch(DuplicateNameException e)
		    {
		        m_out.println(m_formatter.format("login.self.register.duplicate.name"));
			    m_out.println();
		    }
		    catch(AmbiguousNameException e)
		    {
		        m_out.println(m_formatter.format("login.self.register.duplicate.name"));
			    m_out.println();
		    }		    
	    }
	}
	
	public synchronized void shutdown()
	throws UnexpectedException
	{
	    if(m_heartbeatSender != null)
	        m_heartbeatSender.interrupt();
		m_in.shutdown();
		if(m_session != null)
		{
		    try
		    {
		        m_session.close();
		    }
		    catch(Exception e)
		    {
		        // Trying to close an invalid session. Not much
		        // to do really...
		        //
		        Logger.warn(this, "Error while closing session: " + e.toString());
		    }
			m_session = null;
		}
	}
	
	public void executeScript(String script)
	throws IOException, InterruptedException, KOMException
	{
	    this.executeScript(new BufferedReader(new StringReader(script)));
	}
	
	public void executeScript(BufferedReader rdr)
	throws IOException, InterruptedException, KOMException
	{
	    String line;
	    while((line = rdr.readLine()) != null)
	    {
	        line = line.trim();
	        if(line.length() == 0 || line.charAt(0) == '#')
	            continue;
		    ExecutableCommand executableCommand = m_parser.parseCommandLine(this, line);
	        executableCommand.executeBatch(this);	        
	        m_out.println();
	    }
	}
		
	public void mainloop()
	{
    	try
    	{
    		this.printCurrentConference();
    	}
    	catch(ObjectNotFoundException e)
    	{
    		// TODO: Default conference deleted. What do we do???
    		//
    	    Logger.error(this, e);
    		m_out.println(e.formatMessage(this));
    	}
    	catch(UnexpectedException e)
    	{
    	    Logger.error(this, e);
    		m_out.println(e.formatMessage(this));
    	}
    	m_out.println();
    	while (m_loggedIn)
    	{			
    		// Determine default command and print prompt
    		//
    		try
    		{
    			// Print any pending chat messages
    			//
    		    DisplayController dc = this.getDisplayController();
    			synchronized(m_displayMessageQueue)
    			{
    				while(!m_displayMessageQueue.isEmpty())
    				{
    					Event ev = (Event)m_displayMessageQueue.removeFirst();
    					ev.dispatch(eventPrinter);
    				}
    			}
    			Command defaultCommand = this.getDefaultCommand();
    			dc.prompt();
    			
    			// Build prompt
    			//
    			StringBuffer promptBuffer = new StringBuffer(50);
    			promptBuffer.append(defaultCommand.getFullName());
    			if((this.getCachedUserInfo().getFlags1() & UserFlags.SHOW_NUM_UNREAD) != 0
    			        && m_state.getNumUnread() > 0)
    			{
    			    // Add number of unread to prompt
    			    //
    			    promptBuffer.append(" [");
    			    promptBuffer.append(m_state.getNumUnread());
    			    promptBuffer.append(']');
    			}
    			promptBuffer.append(" - ");
    			String prompt = promptBuffer.toString();
    			m_out.print(prompt);
    			dc.input();
    			m_out.flush();
    			
    			// Clear pager line counter
    			//
    			m_in.resetLineCount();
    			
    			// Read command
    			//
    			String cmdString = null;
    			try
    			{
    				cmdString = m_in.readLine("", "", 0, LineEditor.FLAG_ECHO | LineEditor.FLAG_RECORD_HISTORY
    				        | LineEditor.FLAG_STOP_ON_EVENT | LineEditor.FLAG_STOP_ONLY_WHEN_EMPTY | LineEditor.FLAG_ALLOW_HISTORY | 
                            ((this.getCachedUserInfo().getFlags1() & UserFlags.TREAT_SPACE_AS_NEWLINE) != 0 ? LineEditor.FLAG_TREAT_SPACE_AS_NEWLINE : 0));
    			}
    			catch(EventDeliveredException e)
    			{
    				// Interrupted by an event. Generate prompt and start 
    				// over again.
    				//
    				// Erase the prompt
    				//
    			    if((this.getCachedUserInfo().getFlags1() & UserFlags.BEEP_ON_NEW_MESSAGES) != 0 
    			            && (e.getEvent() instanceof NewMessageEvent))
    			        m_out.print('\u0007'); // BEEP!
    				int top = prompt.length();
    				for(int idx = 0; idx < top; ++idx)
    					m_out.print("\b \b");
    				continue;
    			}
    			
    			if(cmdString.trim().length() > 0)
    			{	
    			    ExecutableCommand executableCommand = m_parser.parseCommandLine(this, cmdString);
			        executableCommand.execute(this);
    			}
    			else
    			{
    			    new ExecutableCommand(defaultCommand, new Object[0]).execute(this);
    			}
    		}
    		catch(OutputInterruptedException e)
    		{
    		    m_out.println();
    			m_out.println(e.formatMessage(this));
    			m_out.println();
    		}    		    		
    		catch(UserException e)
    		{
    		    m_out.println();
    			m_out.println(e.formatMessage(this));
    			m_out.println();
    		}
    		catch(InterruptedException e)
    		{
    			// SOMEONE SET UP US THE BOMB! Let's get out of here!
    			// Can happen if connection is lost, or if an admin 
    		    // requested shutdown.
    		    //
    			return;
    		}			
    		catch(ImmediateShutdownException e)
    		{
    		    // SOMEONE SET UP US THE *BIG* BOMB!
    		    //
    		    return;
    		}
    		catch(KOMRuntimeException e)
    		{
    			m_out.println(e.formatMessage(this));
    			m_out.println();
    			Logger.error(this, e);
    		}			    		
    		catch(Exception e)
    		{
    			e.printStackTrace(m_out);
    			m_out.println();
    			Logger.error(this, e);
    		} 
    	}
    }
		
	public Command getDefaultCommand()
	throws KOMException
	{
	    m_state = m_session.getSessionState();
	    short suggestion = m_state.getSuggestedAction(); 
		switch(suggestion)
		{
		    case CommandSuggestions.NEXT_SELECTED:
		        return m_parser.getCommand(ReadNextSelectedMessage.class);
			case CommandSuggestions.NEXT_MAIL:
				return m_parser.getCommand(ReadNextMail.class);
			case CommandSuggestions.NEXT_REPLY:
				return m_parser.getCommand(ReadNextReply.class);
			case CommandSuggestions.NEXT_MESSAGE:
				return m_parser.getCommand(ReadNextMessage.class);
			case CommandSuggestions.NEXT_CONFERENCE:
				return m_parser.getCommand(GotoNextConference.class);
			case CommandSuggestions.NO_ACTION:
				return m_parser.getCommand(ShowTime.class);
			case CommandSuggestions.ERROR:
			    m_out.println("TODO: Add message saying thing are screwed up");
			    return m_parser.getCommand(ShowTime.class);
			default:
				Logger.warn(this, "Unknown command suggestion: " + suggestion);
				return m_parser.getCommand(ShowTime.class);
		}
	}
	
	// Implementation of the Context interface
	//
	public LineEditor getIn()
	{
		return m_in;
	}
	
	public KOMWriter getOut()
	{
		return m_out;
	}
			
	public MessageFormatter getMessageFormatter()
	{
		return m_formatter;
	}
	
	public MessagePrinter getMessagePrinter()
	{
	    boolean usecompact = false;
        try
        {
            usecompact = this.isFlagSet(0, UserFlags.USE_COMPACT_MESSAGEPRINTER);
        } 
        catch (ObjectNotFoundException e)
        {
        } 
        catch (UnexpectedException e)
        {
        }
        if (usecompact)
	    {
	        return new CompactMessagePrinter();
	    }
	    else
	    {
	        return new BasicMessagePrinter();
	    }
	}
	
	public ServerSession getSession()
	{
		return m_session;
	}
	    
    public String formatObjectName(Name name, long id)
    {
        try
        {
        	String nameStr = name.getKind() == NameManager.CONFERENCE_KIND 
        	&& this.getLoggedInUserId() == id 
        		? this.getMessageFormatter().format("misc.mailboxtitle")
        		: name.getName();
        		
        	// Omit user suffixes if requested
            //
        	if(name.getKind() == NameManager.USER_KIND && (this.getCachedUserInfo().getFlags1() & UserFlags.SHOW_SUFFIX) ==0)
        		nameStr = NameUtils.stripSuffix(nameStr);
	        StringBuffer sb = new StringBuffer(nameStr.length() + 10);
	        if(nameStr.length() == 0)
	        {
	            // Protected conference, just show hidden name.
	        	//
	        	nameStr = m_formatter.format("misc.protected.conference");
	            sb.append(nameStr);
	        }
	        else
	        {
	            // Normal conference. Show name and maybe object id.
	        	//
	            sb.append(nameStr);
		        if((this.getCachedUserInfo().getFlags1() & UserFlags.SHOW_OBJECT_IDS) != 0)
		        {
		            sb.append(" <");
		            sb.append(id);
		            sb.append('>');
		        }
	        }
	        return sb.toString();
        }
        catch(UnexpectedException e)
        {
            throw new RuntimeException(e);
        }        
    }

    
    public String formatObjectName(NameAssociation object)
    {
        return object == null
        	? m_formatter.format("misc.nobody")
        	: this.formatObjectName(object.getName(), object.getId());
    }
    
    public String smartFormatDate(Date date)
    throws UnexpectedException
    {
        if(date == null)
            return m_formatter.format("date.never");
        
        // Check number of days from today
        //
        Calendar then = Calendar.getInstance();
        then.setTime(date);
        then.setTimeZone(this.getCachedUserInfo().getTimeZone());
        String answer = m_formatter.format("timestamp.short", then.getTime());
        
        // Should we even try to be smart?
        // 
        if((this.getCachedUserInfo().getFlags1() & UserFlags.ALWAYS_PRINT_FULL_DATE) != 0)
            return answer;
        
        Calendar now = Calendar.getInstance(this.getCachedUserInfo().getTimeZone());
        now.setTimeInMillis(System.currentTimeMillis());
        
        // Today or yesterday?
        //
        // Future date? Format the usual way
        //
        if(now.before(then))
            return answer;
        int yearNow = now.get(Calendar.YEAR);
        int yearThen = then.get(Calendar.YEAR);
        int dayNow = now.get(Calendar.DAY_OF_YEAR);
        int dayThen = then.get(Calendar.DAY_OF_YEAR);
        if(yearNow == yearThen + 1)
            dayNow += then.getActualMaximum(Calendar.DAY_OF_YEAR);
        else
        {
            if(yearNow != yearThen)
                return answer;
        }
        int dayDiff = dayNow - dayThen;
        if(dayDiff == 0)
            answer = m_formatter.format("date.today");
        else if(dayDiff == 1)
       	    answer = m_formatter.format("date.yesterday");
        else if(dayDiff < 7)
        {
        	answer = m_dateSymbols.getWeekdays()[then.get(Calendar.DAY_OF_WEEK)];
        	answer = answer.substring(0, 1).toUpperCase() + answer.substring(1);
        }
        else
            return answer;
        return answer + ", " + m_formatter.format("time.short", then.getTime());
    }
    
    public DisplayController getDisplayController()
    {
        try
        {
	        return (this.getCachedUserInfo().getFlags1() & UserFlags.ANSI_ATTRIBUTES) != 0
	        	? (DisplayController) new ANSIDisplayController(m_out)
	        	: (DisplayController) new DummyDisplayController(m_out);
        }
        catch(UnexpectedException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public TerminalController getTerminalController()
    {
        // TODO: Read from configuration
        //
        return new ANSITerminalController(this.getOut());
    }
			
	public void printCurrentConference()
	throws ObjectNotFoundException, UnexpectedException
	{	
		// Determine name of conference. Give a generic name
		// to mailboxes.
		//
	    this.getDisplayController().input();
		ConferenceInfo conf = m_session.getCurrentConference();
		long id = conf.getId();
		String confName = this.formatObjectName(conf.getName(), id); 
		
		// Calculate number of messages and print greeting
		//
		int numMessages = m_session.countUnread(id);
		m_out.println(m_formatter.format(numMessages == 1 
		        ? "misc.one.unread.message" 
		                : "misc.enter.conference", 
		                new String[] { confName, 
		             numMessages == 0 
		             ? m_formatter.format("misc.no.messages") 
		                     : Long.toString(numMessages) }));
	}
			
	public MessageEditor getMessageEditor()
	throws UnexpectedException
	{
		try
		{
			return (this.getCachedUserInfo().getFlags1() & UserFlags.USE_FULL_SCREEN_EDITOR) != 0
				? (MessageEditor) new FullscreenMessageEditor(this)
		        : (MessageEditor) new SimpleMessageEditor(this);
		}
		catch(IOException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public TerminalSettings getTerminalSettings()
	{
		return new TerminalSettings(m_windowHeight != -1 ? m_windowHeight : 24, m_windowWidth != -1 ? m_windowWidth : 80, "ANSI");
	}
	
    public void setTerminalHeight(int height)
    {
        m_windowHeight = height;
    }
    public void setTerminalWidth(int width)
    {
        m_windowWidth = width;
    }
    
    public void setListenToTerminalSize(boolean value)
    {
        m_ListenToTerminalSize = value;
    }
    
	public void printDebugInfo()
	{
		m_out.println(m_session.getDebugString());
	}		
	
	public long getLoggedInUserId()
	{
		return m_userId;
	}
	
	public synchronized UserInfo getCachedUserInfo()
	throws UnexpectedException
	{
	    try
	    {
			if(m_thisUserCache == null)
				m_thisUserCache = m_session.getUser(this.getLoggedInUserId());
			return m_thisUserCache;
	    }
	    catch(ObjectNotFoundException e)
	    {
	        throw new UnexpectedException(this.getLoggedInUserId(), e);
	    }
	}
	
	public synchronized void clearUserInfoCache()
	{
		m_thisUserCache = null;
	}
	
	public boolean isFlagSet(int flagWord, long mask)
	throws ObjectNotFoundException, UnexpectedException
	{
		return (this.getCachedUserInfo().getFlags()[flagWord] & mask) == mask; 
	}
	
	public WordWrapper getWordWrapper(String content)
	{
		return m_wordWrapperFactory.create(content, this.getTerminalSettings().getWidth());
	}
	
	public WordWrapper getWordWrapper(String content, int width)
	{
		return m_wordWrapperFactory.create(content, width);
	}

	public WordWrapper getWordWrapper(String content, int width, int offset)
	{
		return m_wordWrapperFactory.create(content, width, offset);
	}

	
	public String[] getFlagLabels(String flagTable)
	{
		return this.loadFlagTable(flagTable);
	}
	
	public void checkName(String name)
	throws DuplicateNameException, InvalidNameException, UnexpectedException
	{
	    // Check that name is lexigraphically correct
	    //
	    if(!NameUtils.isValidName(name))
	        throw new InvalidNameException(name);
	    
	    // Check for conflict with mailbox name
	    //
		if (NameUtils.normalizeName(name).equals(NameUtils.normalizeName(m_formatter.format("misc.mailboxtitle"))))
		    throw new DuplicateNameException(name);
		
		// Check for conflict with existing name, ignoring suffixes
		//
		String normalized = NameUtils.stripSuffix(NameUtils.normalizeName(name));
		
		// Nothing left after normalizing name? That's not legal!
		//
		if(normalized.length() == 0)
		    throw new InvalidNameException(name);
		NameAssociation names[] = m_session.getAssociationsForPattern(normalized);
		int top = names.length;
		for(int idx = 0; idx < top; ++idx)
		{
		    String each = names[idx].getName().toString();
		    if(NameUtils.stripSuffix(NameUtils.normalizeName(each)).equals(normalized))
		        throw new DuplicateNameException(name);
		}
	}

	
	// Implementation of EventTarget
	//
	public void onEvent(Event e)
	{
		System.out.println("Unknown Event: " + e.toString());
	}
	
	public void onEvent(ChatMessageEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(event);
		}		
	}

	public void onEvent(BroadcastMessageEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(event);
		}		
	}

	public void onEvent(BroadcastAnonymousMessageEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(event);
		}		
	}

	public void onEvent(ChatAnonymousMessageEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(event);
		}		
	}
	
	public void onEvent(UserAttendanceEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(event);
		}				
	}
	
	public synchronized void onEvent(ReloadUserProfileEvent event)
	{
		// Invalidate user info cache
		//
		m_thisUserCache = null;
	}
	
	public void onEvent(NewMessageEvent event)
	{
	    // Handled in command loop
	}
	
	public void onEvent(MessageDeletedEvent event)
	{
	    // Handled in command loop
	}
	
	public void onEvent(TicketDeliveredEvent event)
	{
	    // TODO: Implement
	}
    
    public void onEvent(DetachRequestEvent event)
    {
        this.detach();
    }
	
	// Implementation of TerminalSizeListener
	//
	public void terminalSizeChanged(int width, int height)
	{
	    if (m_ListenToTerminalSize)
	    {
	        m_windowWidth = width;
			m_windowHeight = height;
	    }
	}
	
	public void environmentChanged(String name, String value)
	{
	    if(!"TICKET".equals(name))
	        return;
	    Logger.debug(this, "Received ticket");
	    m_in.handleEvent(new TicketDeliveredEvent(this.getLoggedInUserId(), value));
	}
	
	protected String waitForTicket()
	throws InterruptedException, IOException
	{
	    // Do we already have a ticket?
	    //
	    if(m_ticket != null)
	        return m_ticket;
	    for(;;)
	    {
		    try
		    {
		        m_in.readLine("", "", 0, LineEditor.FLAG_STOP_ON_EVENT);
		    }
		    catch(EventDeliveredException e)
		    {
		        Logger.debug(this, "Got event while waiting for ticket: " + e.getClass().getName());
		        Event ev = e.getEvent();
		        if(ev instanceof TicketDeliveredEvent)
		            return ((TicketDeliveredEvent) ev).getTicket();
		    }
		    catch(LineEditingDoneException e)
		    {
		        throw new RuntimeException("This should not happen!", e);
		    }		    
		    catch(LineUnderflowException e)
		    {
		        // Ignore
		    }
		    catch(LineOverflowException e)
		    {
		        // Ignore
		    }
		    catch(StopCharException e)
		    {
		        // Ignore
		    }
		    catch(OperationInterruptedException e)
		    {
		        throw new InterruptedException();
		    }
	    }
	}
	
	protected void installCommands()
	throws UnexpectedException
	{
		
		try
		{
			m_parser = Parser.load("commands.xml", this);
		}
		catch(IOException e)
		{
			throw new UnexpectedException(-1, e);
		}
	}
	
	public String[] loadFlagTable(String prefix)
	{
		String[] flagLabels = new String[UserFlags.NUM_FLAGS];
		for(int idx = 0; idx < UserFlags.NUM_FLAGS; ++idx)
		{
			// Calculate flag word and flag bit index
			//
			int flagWord = 1 + (idx / 64);
			long flagBit = (long) 1 << (long) (idx % 64);
			String hex = Long.toHexString(flagBit);
			
			// Build message key
			//
			StringBuffer buf = new StringBuffer();
			buf.append(prefix);
			buf.append('.');
			buf.append(flagWord);
			buf.append('.');
			int top = 8 - hex.length();
			for(int idx2 = 0; idx2 < top; ++idx2)
				buf.append('0');
			buf.append(hex);
			
			// Get flag label
			//
			flagLabels[idx] = m_formatter.getStringOrNull(buf.toString());			
		}
		return flagLabels;
	}
	
	/**
	 * Returns an array of all Commands that are available to the user.
	 * 
	 * @return An Command[] of available commands.
	 */
	public Command[] getCommandList()
	{
		return m_parser.getCommandList();
	}

	public Parser getParser()
	{
	    return m_parser;
	}
	
	public void setTicket(String ticket)
	{
	    m_ticket = ticket;
    }
    
    public void detach() 
    {
        m_session = null;
        m_loggedIn = false;
    }
	
    public void logout() 
    {
        m_loggedIn = false;
    }
    
}
