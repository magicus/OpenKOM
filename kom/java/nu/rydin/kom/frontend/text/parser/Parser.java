/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import nu.rydin.kom.UnexpectedException;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.CommandParser;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author Henrik
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Parser
{
	private Command[] m_commands;
	
	/** Map[Command->CommandLinePart[]] */
	private Map m_commandToPartsMap = new HashMap();

	private class CommandToMatches {
		private Command m_command;
		/** List[CommandLinePart.Match] */
		private List m_matches = new LinkedList();
		/**
		 * @param command
		 * @param matches
		 */
		public CommandToMatches(Command command)
		{
			m_command = command;
		}
		public Command getCommand()
		{
			return m_command;
		}
		public List getMatches()
		{
			return m_matches;
		}
		public void addMatch(Match match) {
			m_matches.add(match);
		}
		/**
		 * @param level
		 * @return
		 */
		public Match getMatch(int level)
		{
			return (Match) m_matches.get(level);
		}
		/**
		 * @param level
		 * @return
		 */
		public CommandLinePart getCommandLinePart(int level)
		{
			return ((CommandLinePart[]) (m_commandToPartsMap.get(m_command)))[level];
		}
	}
	
	/**
	 * @param commands
	 * @param primaryCommands
	 */
	public Parser(Command[] commands, Map commandNames)
	{
		m_commands = commands;
		
		// Initialize the command->parts map with a pair for each command
		// and its corresponding array of command line parts (command name followed
		// by its signature).
		for (int i = 0; i < commands.length; i++)
		{
			Command command = commands[i];
			String name = (String)commandNames.get(command);
			CommandNamePart[] nameParts = splitName(name);
			CommandLineParameter[] parameterParts = command.getSignature();
			
			CommandLinePart[] commandLineParts = new CommandLinePart[nameParts.length + 
																	 parameterParts.length];
			System.arraycopy(commandLineParts, 0, nameParts, 0, nameParts.length);
			System.arraycopy(commandLineParts, nameParts.length, parameterParts, 0, parameterParts.length);
			m_commandToPartsMap.put(command, commandLineParts);
		}
	}

	/**
	 * @param name
	 * @return
	 */
	private CommandNamePart[] splitName(String name)
	{
		String cooked = CommandLinePart.cookString(name);
		String[] cookedParts = cooked.split(" ");
		CommandNamePart[] result = new CommandNamePart[cookedParts.length];
		for (int i = 0; i < cookedParts.length; i++)
		{
			String cookedPart = cookedParts[i];
			result[i] = new CommandNamePart(cookedPart);
		}
		return result;
	}

	public void parseAndExecute(Context context, String commandLine) throws IOException, InterruptedException
	{
		int level = 0;
		
		// List[CommandToMatches] 
		List potentialTargets = new LinkedList();
		
		// Build a copy of all commands first, to filter down later.
		for (int i = 0; i < m_commands.length; i++)
		{
			potentialTargets.add(new CommandToMatches(m_commands[i]));
		}
		
		while (potentialTargets.size() > 1)
		{
			for (Iterator iter = potentialTargets.iterator(); iter.hasNext();)
			{
				CommandToMatches potentialTarget = (CommandToMatches)iter.next();
				CommandLinePart part = potentialTarget.getCommandLinePart(level);
				String commandLineToMatch;
				if (level == 0) {
					commandLineToMatch = commandLine;
				} else {
					commandLineToMatch = potentialTarget.getMatch(level).getRemainder();
				}
				Match match = part.match(commandLine);
				if (!match.isMatching()) {
					potentialTargets.remove(potentialTarget);
				} else {
					potentialTarget.addMatch(match);
				}
			}
			level++;
		}
		
		// Now we either have one target candidate, or none.
		if (potentialTargets.size() == 0) {
			// No matching command found. Print error and abort.
			PrintWriter out = context.getOut();
			MessageFormatter fmt = context.getMessageFormatter();
			
			out.println(fmt.format("parser.unknown", commandLine));
			out.flush();
			return;
		} else {
			// We have one match, but it is not neccessarily correct: we might have
			// too few parameters, as well as too many. Let's find out, and
			// ask user about missing parameters.
			
			CommandToMatches target = (CommandToMatches) potentialTargets.get(0); 
			CommandLinePart[] parts = (CommandLinePart[]) m_commandToPartsMap.get(target.getCommand());
			Match lastMatch = target.getMatch(level - 1);
			
			// First, do we have more left on the command line to parse?
			// If so, match and put it in the targets match list.
			String remainder = lastMatch.getRemainder();
			while (remainder.length() > 0) {
				if (level < parts.length) {
					// We still have parts to match to
					Match match = parts[level].match(remainder);
					if (!match.isMatching()) {
						// User have entered an invalid parameter. Report error.
						//FIXME
						return;
					}
					target.addMatch(match);
					remainder = match.getRemainder();
					level++;
				} else {
					// User have entered too many parameters. Report error.
					//FIXME
					return;
				}
			}
			
			// Now, resolve the entered parts.
			List resolvedParameters = new LinkedList();
			int i = 0;
			for (Iterator iter = target.getMatches().iterator(); iter.hasNext();)
			{
				Match match = (Match)iter.next();
				// If this is a command name part, then it is part of the
				// signature. Add the resolved value of the match to our parameter
				// list.
				if (parts[i] instanceof CommandLineParameter) {
					Object parameter = parts[i].resolveFoundObject(context, match);
					if (parameter == null) {
						// Error message have already been written. User aborted.
						return;
					}
					resolvedParameters.add(parameter);
				}
			}
			
			// If we still need more parameters, ask the user about them.
			while (level < parts.length) {
				// Not on command line, ask the user about it.
				Match match = parts[level].fillInMissingObject(context);
				if (!match.isMatching()) {
					// The user entered an invalid parameter, abort
					PrintWriter out = context.getOut();
					MessageFormatter fmt = context.getMessageFormatter();
					
					out.println(fmt.format("parser.invalid.parameter"));
					out.flush();
					return;
				}
				
				// Resolve directly
				Object parameter = parts[i].resolveFoundObject(context, match);
				if (parameter == null) {
					// Error message have already been written. User aborted.
					return;
				}
				resolvedParameters.add(parameter);
			}
			
			// Now we can execute the command with the resolved parameters.
			Object[] parameterArray = new Object[resolvedParameters.size()];
			resolvedParameters.toArray(parameterArray);
			target.getCommand().execute2(context, parameterArray);
		}
	}
	
	private static final Class[] s_commandCtorSignature = new Class[] { String.class };
	
	public static Parser load(String filename, MessageFormatter formatter)
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
			return new Parser(commands, primaryCommands);
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
	

	
}
