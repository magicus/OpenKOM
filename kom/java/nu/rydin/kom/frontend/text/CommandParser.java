/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nu.rydin.kom.OperationInterruptedException;
import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * Command line parser. Does what you think it does.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class CommandParser
{
	/**
	 * The commands we support
	 */
	private final Command[] m_commandList;

	private Map m_primaryCommands = new HashMap();
	
	private static final Class[] s_commandCtorSignature = new Class[] { String.class };	
	
	/**
	 * Constructs a new command parser
	 * 
	 * @param commandList
	 */
	public CommandParser(Command[] commandList)
	{
		m_commandList = commandList;
	}
	
	public static CommandParser load(String filename, MessageFormatter formatter)
	throws IOException, UnexpectedException
	{
		try
		{
			Map primaryCommands = new HashMap();
			List list = new ArrayList();
			BufferedReader rdr = new BufferedReader(
				new InputStreamReader(CommandParser.class.getResourceAsStream(filename)));
				
			// Read command list
			//
			String line;
			while((line = rdr.readLine()) != null)
			{
				line = line.trim();
				if(!line.startsWith("#"))
					list.add(line);
			}
			rdr.close();
				
			// Instantiate commands
			//
			int top = list.size();
			List commandList = new ArrayList();
			for(int idx = 0; idx < top; ++idx)
			{
				Class clazz = Class.forName((String) list.get(idx));
				Constructor ctor = clazz.getConstructor(s_commandCtorSignature); 
					
				// Install primary command
				//
				String name = formatter.format(clazz.getName() + ".name");
				Command primaryCommand = (Command) ctor.newInstance(new Object[] { name });
				commandList.add(primaryCommand);
				primaryCommands.put(clazz, primaryCommand); 
					
				// Install aliases
				//
				int aliasIdx = 1;
				for(;; ++aliasIdx)
				{
					// Try alias key
					//
					String alias = formatter.getStringOrNull(clazz.getName() + ".name." + aliasIdx);
					if(alias == null)
						break; // No more aliases
						
					// We found an alias! Create command.
					//
					commandList.add(ctor.newInstance(new Object[] { alias }));
				}
			}
				
			// Copy to command array
			// 
			Command[] commands = new Command[commandList.size()];
			commandList.toArray(commands);
			return new CommandParser(commands, primaryCommands);
		}
		catch(ClassNotFoundException e)
		{
			throw new UnexpectedException(-1, e);
		}
		catch(NoSuchMethodException e)
		{
			throw new UnexpectedException(-1, e);
		}
		catch(InstantiationException e)
		{
			throw new UnexpectedException(-1, e);
		}
		catch(IllegalAccessException e)
		{
			throw new UnexpectedException(-1, e);
		}
		catch(InvocationTargetException e)
		{
			throw new UnexpectedException(-1, e.getCause());
		}		
	}
	
	public CommandParser(Command[] commands, Map primaryCommands)
	{
		m_commandList		= commands;
		m_primaryCommands 	= primaryCommands;
	}
	
	/**
	 * Parses a command line and returns a command.
	 * 
	 * @param context The context. Used for IO and formatting
	 * @param command The command, as a literal string
	 * @param parts The command, split up into an array of words
	 * @return The resulting <tt>Command</tt> object
	 * @throws IOException 
	 * @throws InterruptedException
	 * @throws OperationInterruptedException
	 */
	public Command parse(Context context, String command, String[] parts)
	throws IOException, InterruptedException, OperationInterruptedException
	{	
		LineEditor in = context.getIn();
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		for(;;)
		{
			// Collect matching commands
			//
			int top = m_commandList.length;
			List l = new ArrayList();
			for(int idx = 0; idx < top; ++idx)
			{
				Command c = m_commandList[idx];
				if(c.match(parts) > 0)
					l.add(c);
			}
			
			// Ambiguous, exactly one or no match at all?
			//
			switch(l.size())
			{
				case 0:
					// No match. The command was unknown.
					//
					out.println();
					out.println(formatter.format("parser.unknown", command));
					out.println();
					return null;
				case 1:
					// Exactly one match. Go run it!
					//
					return (Command) l.get(0);
				default:
					// More than one match.
					//
					// It may still be unique, though, since we consider the "best"
					// match being the one with the most matching parts. If there's
					// only one command having the best score, we pick that one.
					//
					Command best = null;
					int bestScore = 0;
					int tie = 0;
					Command each = null;
					for(Iterator itor = l.iterator(); itor.hasNext();)
					{
						each = (Command) itor.next(); 
						int n = each.match(parts);
						if(n > bestScore)
						{
							best = each; 
							bestScore = n;
							tie = 0;			 
						}
						else if(n == bestScore)
							++tie;
					}
					// Only one command with best score? Pick that one!
					//
					if(tie == 0)
						return each; 
					
					// Ask user to chose
					//
					out.println(formatter.format("parser.ambiguous"));
					int top2 = l.size();
					for(int idx = 0; idx < top2; ++idx)
					{
						out.print(idx + 1);
						out.print(". ");
						out.println(((Command) l.get(idx)).getFullName()); 
					}
					out.print(formatter.format("parser.chose"));
					out.flush();
					String input = in.readLine();
					
					// Empty string given? Abort!
					//
					if(input.length() == 0)
						throw new OperationInterruptedException();
						
					int idx = 0;
					try
					{
						idx = Integer.parseInt(input);
					}
					catch(NumberFormatException e)
					{
						// Could not interpret as number. Try
						// parsing it again!
						//
						command = input;
						parts = NameUtils.splitName(input);
						continue; 
					}
					if(idx < 1 || idx > top2 + 1)
					{
						out.println(formatter.format("parser.invalid.choice"));
						return null;
					}
					return (Command) l.get(idx - 1);
			}
		}	
	}
	
	public Command[] getCommandList()
	{
		return m_commandList;
	}
	
	public Command getCommand(Class wantedClass)
	{
		return (Command) m_primaryCommands.get(wantedClass);
	}
}
