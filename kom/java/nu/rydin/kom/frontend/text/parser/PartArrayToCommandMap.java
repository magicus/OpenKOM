/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.frontend.text.Command;

/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class PartArrayToCommandMap
{
	private Command m_command;

	private CommandLinePart[] m_commandLineParts;

	public PartArrayToCommandMap(Command command,
			CommandLinePart[] commandLineParts)
	{
		m_command = command;
		m_commandLineParts = commandLineParts;
	}
	
	public Command getCommand()
	{
		return m_command;
	}

	public CommandLinePart[] getCommandLineParts()
	{
		return m_commandLineParts;
	}

}