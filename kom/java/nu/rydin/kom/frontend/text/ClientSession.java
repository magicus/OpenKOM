/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import nu.rydin.kom.AlreadyLoggedInException;
import nu.rydin.kom.AuthenticationException;
import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.EventDeliveredException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.KOMRuntimeException;
import nu.rydin.kom.ObjectNotFoundException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.UserException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.backend.ServerSessionFactoryImpl;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.events.BroadcastAnonymousMessageEvent;
import nu.rydin.kom.events.BroadcastMessageEvent;
import nu.rydin.kom.events.ChatAnonymousMessageEvent;
import nu.rydin.kom.events.ChatMessageEvent;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.events.EventTarget;
import nu.rydin.kom.events.MessageDeletedEvent;
import nu.rydin.kom.events.NewMessageEvent;
import nu.rydin.kom.events.ReloadUserProfileEvent;
import nu.rydin.kom.events.UserAttendanceEvent;
import nu.rydin.kom.frontend.text.commands.GotoNextConference;
import nu.rydin.kom.frontend.text.commands.Logout;
import nu.rydin.kom.frontend.text.commands.ReadNextMessage;
import nu.rydin.kom.frontend.text.commands.ReadNextReply;
import nu.rydin.kom.frontend.text.commands.ShowTime;
import nu.rydin.kom.frontend.text.editor.StandardWordWrapper;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.editor.WordWrapperFactory;
import nu.rydin.kom.frontend.text.editor.simple.SimpleEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ClientSession implements Runnable, Context, EventTarget, TerminalSizeListener
{
	private static final int MAX_LOGIN_RETRIES = 3;
	private static final String DEFAULT_CHARSET = "US-ASCII";
	
	private LineEditor m_in;
	private KOMWriter m_out;
	private final InputStream m_rawIn;
	private final OutputStream m_rawOut; 
	private MessageFormatter m_formatter = new MessageFormatter();
	private ServerSession m_session;
	private long m_userId;
	private LinkedList m_displayMessageQueue = new LinkedList();
	private UserInfo m_thisUserCache;
	private String[] m_flagLabels;
	private WordWrapperFactory m_wordWrapperFactory = new StandardWordWrapper.Factory();
	private int m_windowHeight = -1;
	private int m_windowWidth = -1;

	
	// This could be read from some kind of user config if the
	// user wants an alternate message printer. 
	//
	private final MessagePrinter m_messagePrinter = new BasicMessagePrinter();

	private CommandParser m_parser;	
		
	private static class ListCommands extends AbstractCommand
	{
		public ListCommands(String fullName)
		{
			super(fullName);
		}
		
		public void execute(Context context, String[] args)
		{
			PrintWriter out = context.getOut();
			Command[] cmds = ((ClientSession) context).m_parser.getCommandList();
			int top = cmds.length;
			for(int idx = 0; idx < top; ++idx)
				out.println(cmds[idx].getFullName());
		}
	}
	
	public ClientSession(InputStream in, OutputStream out)
	throws UnexpectedException
	{
		// Set up I/O
		//
		m_rawIn = in;
		m_rawOut = out;
		
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
		
		// Set up flag table
		//
		this.loadFlagTable();
	}
	 
	public void run()
	{ 
		try
		{
		    // Start keystroke poller
		    //
		    m_in.start();

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
						Thread.currentThread().setName("Session " + userInfo.getUserid());
						break;
					}
					catch(AuthenticationException e)
					{
						new PrintStream(m_rawOut).println(m_formatter.format("login.failure"));
					}
				}
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
				
			m_out.println(m_formatter.format("login.welcome", userInfo.getName()));
			m_out.println();
				
			userInfo = null; // Don't need it anymore... Let it be GC'd
			
			// Enter main command loop
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
	throws AuthenticationException, AuthorizationException, UnexpectedException, IOException, InterruptedException
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
				throw new AuthorizationException();
				
			// Everything seems fine! We're in!
			//
			m_in.setSession(m_session);
			return user;
		}
	}
	
	public synchronized void shutdown()
	throws UnexpectedException
	{
		m_in.shutdown();
		if(m_session != null)
		{
			m_session.close();
			m_session = null;
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
			m_out.println(e.formatMessage(this));
		}
		catch(UnexpectedException e)
		{
			m_out.println(e.formatMessage(this));
		}
		m_out.println();
		for(;;)
		{			
			// Determine default command and print prompt
			//
			try
			{
				// Print any pending chat messages
				//
				synchronized(m_displayMessageQueue)
				{
					while(!m_displayMessageQueue.isEmpty())
					{
						m_out.println((String) m_displayMessageQueue.removeFirst());
						m_out.println();
					}
				}
				Command defaultCommand = this.getDefaultCommand();
				String prompt = defaultCommand.getFullName() + " - "; 
				m_out.print(prompt);
				m_out.flush();
				
				// Read command
				//
				String cmdString = null;
				try
				{
					cmdString = m_in.readLineStopOnEvent().trim();
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
				
				if(cmdString.length() > 0)
				{
					// Command given. Parse it!
					//
					String[] parts = NameUtils.splitName(cmdString);
					Command command = m_parser.parse(this, cmdString, parts);
					if(command != null) 
					{
						// Time to execute the command, but first, isolate the 
						// parameters.
						//
						String[] args = command.getParameters(parts);

						// Go ahead and execute it!
						//
						this.executeCommand(command, args);
					}
						
					// Special case: Seeing a Logout command here
					// means we should end the loop
					//
					if(command instanceof Logout)
						break;
				}
				else
					this.executeCommand(defaultCommand, new String[0]);
			}
			catch(UserException e)
			{
				m_out.println(e.getMessage());
				m_out.println();
			}
			catch(ObjectNotFoundException e)
			{
				m_out.println(m_formatter.format("error.object.not.found", e.getMessage()));
			}
			catch(UnexpectedException e)
			{
				m_out.println(e.formatMessage(this));
				m_out.println();
				System.err.println("Error caused by user: " + e.getUser());
				e.printStackTrace(System.err);
			}
			catch(KOMException e)
			{
				m_out.println(e.formatMessage(this));
				m_out.println();
			}
			catch(KOMRuntimeException e)
			{
				m_out.println(e.formatMessage(this));
				m_out.println();
			}			
			catch(InterruptedException e)
			{
				// Someone wants us dead. Let's get out of here!
				//
				return;
			}			
			catch(Exception e)
			{
				e.printStackTrace(m_out);
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
			
	public void printCurrentConference()
	throws ObjectNotFoundException, UnexpectedException
	{	
		// Determine name of conference. Give a generic name
		// to mailboxes.
		//
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
			return new SimpleEditor(m_formatter);
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
	throws UnexpectedException, ObjectNotFoundException
	{
		if(m_thisUserCache == null)
			m_thisUserCache = m_session.getUser(this.getLoggedInUserId());
		return m_thisUserCache;
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
		
	public String[] getFlagLabels()
	{
		return m_flagLabels;
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
			m_displayMessageQueue.addLast(m_formatter.format("event.chat", 
								new Object[] { event.getUserName(), event.getMessage() }));
		}		
	}

	public void onEvent(BroadcastMessageEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(m_formatter.format("event.broadcast.default", 
								new Object[] { event.getUserName(), event.getMessage() }));
		}		
	}

	public void onEvent(BroadcastAnonymousMessageEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(m_formatter.format("event.broadcast.anonymous", 
								new Object[] { event.getMessage() }));
		}		
	}

	public void onEvent(ChatAnonymousMessageEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(m_formatter.format("event.broadcast.anonymous", 
								new Object[] { event.getMessage() }));
		}		
	}
	
	public void onEvent(UserAttendanceEvent event)
	{
		// Put it on a queue until there's a good time to display it!
		//
		synchronized(m_displayMessageQueue)
		{
			m_displayMessageQueue.addLast(m_formatter.format("event.attendance." + event.getType(), 
								new Object[] { event.getUserName() }));
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
			m_parser = CommandParser.load("/commands.list", m_formatter);
		}
		catch(IOException e)
		{
			throw new UnexpectedException(-1, e);
		}
	}
	
	protected void executeCommand(Command command, String[] parameters)
	throws InterruptedException, IOException, KOMException
	{
		PrintWriter out = this.getOut();
		command.printPreamble(out);
		command.execute(this, parameters);
		command.printPostamble(out);
	}
	
	public void loadFlagTable()
	{
		m_flagLabels = new String[UserFlags.NUM_FLAGS];
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
			buf.append("userflags.");
			buf.append(flagWord);
			buf.append('.');
			int top = 8 - hex.length();
			for(int idx2 = 0; idx2 < top; ++idx2)
				buf.append('0');
			buf.append(hex);
			
			// Get flag label
			//
			m_flagLabels[idx] = m_formatter.getStringOrNull(buf.toString());			
		}
	}
}
