/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.*;
import java.text.DateFormatSymbols;
import java.util.*;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.ServerSessionFactoryImpl;
import nu.rydin.kom.constants.SystemFiles;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.events.*;
import nu.rydin.kom.exceptions.*;
import nu.rydin.kom.frontend.text.commands.*;
import nu.rydin.kom.frontend.text.editor.StandardWordWrapper;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.editor.WordWrapperFactory;
import nu.rydin.kom.frontend.text.editor.simple.SimpleMessageEditor;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.frontend.text.parser.Parser.ExecutableCommand;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.*;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 * @author Henrik Schröder
 * @author Magnus Ihse
 */
public class ClientSession implements Runnable, Context, EventTarget, TerminalSizeListener
{
	private static final int MAX_LOGIN_RETRIES = 3;
	private static final String DEFAULT_CHARSET = "US-ASCII";
	
	private LineEditor m_in;
	private KOMWriter m_out;
	private final InputStream m_rawIn;
	private final OutputStream m_rawOut; 
	private MessageFormatter m_formatter = new MessageFormatter(Locale.getDefault());
	private ServerSession m_session;
	private long m_userId;
	private LinkedList m_displayMessageQueue = new LinkedList();
	private UserInfo m_thisUserCache;
	private String[] m_flagLabels;
	private String[] m_privLabels;
	private WordWrapperFactory m_wordWrapperFactory = new StandardWordWrapper.Factory();
	private int m_windowHeight = -1;
	private int m_windowWidth = -1;
	private EventPrinter eventPrinter = new EventPrinter();
	private Locale m_locale;
	private DateFormatSymbols m_dateSymbols;
	
	// This could be read from some kind of user config if the
	// user wants an alternate message printer. 
	//
	private final MessagePrinter m_messagePrinter = new BasicMessagePrinter();

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
    
    private HeartbeatSender m_heartbeatSender = new HeartbeatSender();
    
	private class EventPrinter implements EventTarget
	{
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
		}
	
		public void onEvent(ChatMessageEvent event) 
		{
			getDisplayController().normal();
			String header = m_formatter.format("event.chat", new Object[] { event.getUserName() }); 
			m_out.print(header);
			getDisplayController().chatMessageBody();
			printWrapped(event.getMessage(), header.length());
			m_out.println();
		}

		public void onEvent(BroadcastMessageEvent event) 
		{
			getDisplayController().normal();
			String header = m_formatter.format("event.broadcast.default", new Object[] { event.getUserName() }); 
			m_out.print(header);
			getDisplayController().broadcastMessageBody();
			printWrapped(event.getMessage(), header.length());
			m_out.println();
		}

		public void onEvent(ChatAnonymousMessageEvent event) 
		{
			getDisplayController().normal();
			String header = m_formatter.format("event.broadcast.anonymous");
			m_out.print(header);
			getDisplayController().broadcastMessageBody();
			printWrapped(event.getMessage(), header.length());
			m_out.println();			
		}

		public void onEvent(BroadcastAnonymousMessageEvent event) 
		{
			getDisplayController().normal();
			String header = m_formatter.format("event.broadcast.anonymous");
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
			getDisplayController().normal();
			m_out.println(m_formatter.format("event.attendance." + event.getType(), new Object[] { event.getUserName() }));
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
	}
	
	public ClientSession(InputStream in, OutputStream out)
	throws UnexpectedException
	{
		// Set up I/O
		//
		m_rawIn = in;
		m_rawOut = out;
		
		// Set up flag tables
		//
		m_flagLabels = this.loadFlagTable("userflags");
		m_privLabels = this.loadFlagTable("userprivs");
		
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
			throw new UnexpectedException(-1, "US-ASCII not supported. Your JVM is broken!");
		}		
	}
	 
	public void run()
	{ 
		try
		{
		    // Start keystroke poller
		    //
		    m_in.start();
		    m_in.setKeystrokeListener(m_heartbeatSender);

			// Try to login
			//
			UserInfo userInfo = null;
			try
			{
				for(int idx = 0; idx < 3; ++idx)
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
						new PrintStream(m_rawOut).println(m_formatter.format("login.failure"));
					}
				}
			}
			catch(LoginNotAllowedException e)
			{
			    m_out.println(e.getMessage());
			}
			catch(InterruptedException e)
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
				e.printStackTrace();
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
			    m_formatter = new MessageFormatter(m_locale);
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
					    m_out.print(m_formatter.format("login.chose.profile"));
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
				if(profileScript != null)
				    this.executeScript(profileScript);
				if(loginScript != null)
				    this.executeScript(loginScript);
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
			m_heartbeatSender.start();
			
			// MAIN SCREEN TURN ON!
			//		
			this.mainloop();
			m_out.println(m_formatter.format("login.goodbye", m_session.getLoggedInUser().getName()));			
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
				m_out.println(m_formatter.format("logout.failure"));
			}
		}
	}

	protected UserInfo login()
	throws AuthenticationException, LoginNotAllowedException, UnexpectedException, IOException, 
	InterruptedException, OperationInterruptedException
	{
		// Collect information
		//
		m_out.print(m_formatter.format("login.user"));
		m_out.flush();
		String userid = m_in.readLine();
		m_out.print(m_formatter.format("login.password"));
		m_out.flush();
		String password = m_in.readPassword();
		
		for(;;)
		{
			// Authenticate
			//
			ServerSessionFactoryImpl ssf = ServerSessionFactoryImpl.instance();
			try
			{
				m_session = ssf.login(userid, password);
			}
			catch(AlreadyLoggedInException e)
			{
				// Already logged in. Ask user if they want to kill the previous
				// session.
				//
				m_out.print(m_formatter.format("login.terminate.session"));
				m_out.flush();
				String answer = m_in.readLine().toUpperCase();
				
				// If the user didn't let us kill the other session, we're
				// out of here!
				//
				if(answer.length() == 0 || !m_formatter.format("misc.yes").toUpperCase().startsWith(answer))
					throw new InterruptedException();
					
				// Ask server to shut down the other session
				//
				ssf.requestShutdown(userid, password);
				
				// Try to login again
				//
				continue;
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
			return user;
		}
	}
	
	public synchronized void shutdown()
	throws UnexpectedException
	{
	    m_heartbeatSender.interrupt();
		m_in.shutdown();
		if(m_session != null)
		{
			m_session.close();
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
    			String prompt = defaultCommand.getFullName() + " - "; 
    			m_out.print(prompt);
    			dc.input();
    			m_out.flush();
    			
    			// Read command
    			//
    			String cmdString = null;
    			try
    			{
    				cmdString = m_in.readLine("", "", 0, LineEditor.FLAG_ECHO | LineEditor.FLAG_RECORD_HISTORY
    				        | LineEditor.FLAG_STOP_ON_EVENT | LineEditor.FLAG_STOP_ONLY_WHEN_EMPTY);
    			}
    			catch(EventDeliveredException e)
    			{
    				// Interrupted by an event. Generate prompt and start 
    				// over again.
    				//
    				// Erase the prompt
    				//
    				int top = prompt.length();
    				for(int idx = 0; idx < top; ++idx)
    					m_out.print("\b \b");
    				continue;
    			}
    			
    			if(cmdString.trim().length() > 0)
    			{	
    			    ExecutableCommand executableCommand = m_parser.parseCommandLine(this, cmdString);
    			    //executableCommand.getCommand().printPreamble(getOut());
			        executableCommand.execute(this);
			        //executableCommand.getCommand().printPostamble(getOut());
    			}
    			else
    			{
    			    new ExecutableCommand(defaultCommand, new Object[0]).execute(this);
    			}
    		}
    		catch(OutputInterruptedException e)
    		{
    			m_out.println(e.formatMessage(this));
    			m_out.println();
    		}    		    		
    		catch(UserException e)
    		{
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
		switch(m_session.suggestNextAction())
		{
			case ServerSession.NEXT_REPLY:
				return m_parser.getCommand(ReadNextReply.class);
			case ServerSession.NEXT_MESSAGE:
				return m_parser.getCommand(ReadNextMessage.class);
			case ServerSession.NEXT_CONFERENCE:
				return m_parser.getCommand(GotoNextConference.class);
			case ServerSession.NO_ACTION:
				return m_parser.getCommand(ShowTime.class);
			default:
				// TODO: Print warning
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
		return m_messagePrinter;
	}
	
	public ServerSession getSession()
	{
		return m_session;
	}
	
    public String formatObjectName(nu.rydin.kom.structs.Name name, long id)
    {
        return this.formatObjectName(name.getName(), id);
    }
    
    public String formatObjectName(String name, long id)
    {
        try
        {
	        StringBuffer sb = new StringBuffer(name.length() + 10);
	        if(name.length() == 0)
	            name = m_formatter.format("misc.protected.conference");
	        sb.append(name);
	        if((this.getCachedUserInfo().getFlags1() & UserFlags.SHOW_OBJECT_IDS) != 0)
	        {
	            sb.append(' ');
	            sb.append('<');
	            sb.append(id);
	            sb.append('>');
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
	        	: (DisplayController) new DummyDisplayController();
        }
        catch(UnexpectedException e)
        {
            throw new RuntimeException(e);
        }
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
		String confName = id == m_session.getLoggedInUserId()
			? m_formatter.format("misc.mailboxtitle")
			: conf.getName();
		
		// Calculate number of messages and print greeting
		//
		int numMessages = m_session.countUnread(id);
		m_out.println(m_formatter.format("misc.enter.conference", 
			new String[] { confName, 
					numMessages == 0 
						? m_formatter.format("misc.no.messages")
						: Long.toString(numMessages) }));
	}
			
	public MessageEditor getMessageEditor()
	throws UnexpectedException
	{
		// TODO: Determine editor class by reading user config
		//
		try
		{
			return new SimpleMessageEditor(this);
		}
		catch(IOException e)
		{
			throw new UnexpectedException(this.getLoggedInUserId(), e);
		}
	}
	
	public TerminalSettings getTerminalSettings()
	{
		// TODO: Get from telnet session etc.
		//
		return new TerminalSettings(m_windowHeight != -1 ? m_windowHeight : 24, m_windowWidth != -1 ? m_windowWidth : 80, "ANSI");
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

	
	public String[] getFlagLabels()
	{
		return m_flagLabels;
	}
	
	// TODO: Ihse -- fulkod, I know. Vågade inte peta på de gamla funktionerna...
	public String[] getExistingFlagLabels()
	{
	    int numFlags;
	    for (numFlags = 0; numFlags < m_flagLabels.length; numFlags++)
        {
            if (m_flagLabels[numFlags] == null) {
                break;
            }
        }
	    
	    String[] existingFlags = new String[numFlags];
	    System.arraycopy(m_flagLabels, 0, existingFlags, 0, numFlags);
		return existingFlags;
	}
	
	public String[] getRightsLabels()
	{
	    return m_privLabels;
	}
	
	// TODO: Ihse -- fulkod, I know. Vågade inte peta på de gamla funktionerna...
	public String[] getExistingRightsLabels()
	{
	    int numFlags;
	    for (numFlags = 0; numFlags < m_privLabels.length; numFlags++)
        {
            if (m_privLabels[numFlags] == null) {
                break;
            }
        }
	    
	    String[] existingFlags = new String[numFlags];
	    System.arraycopy(m_privLabels, 0, existingFlags, 0, numFlags);
		return existingFlags;
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
		// Not much to do. Command loop will reevaluate default command
		// and try again.
		//
	}
	
	public void onEvent(MessageDeletedEvent event)
	{
		//
	}
	
	// Implementation of TerminalSizeListener
	//
	public void terminalSizeChanged(int width, int height)
	{
		m_windowWidth = width;
		m_windowHeight = height;
	}
	
	protected void installCommands()
	throws UnexpectedException
	{
		
		try
		{
			m_parser = Parser.load("/commands.list", this);
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
	
    public void logout() 
    {
        m_loggedIn = false;
    }
}
