/*
 * Created on Nov 13, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nu.rydin.kom.OperationInterruptedException;
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
	
	/**
	 * Constructs a new command parser
	 * 
	 * @param commandList
	 */
	public CommandParser(Command[] commandList)
	{
		m_commandList = commandList;
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
		// TODO: Fulkod!
		//
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
					out.println(formatter.format("parser.unknown", command));
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


}
