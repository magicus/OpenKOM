/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class AbstractCommand implements Command
{
	protected String m_fullName;
	protected String[] m_nameParts;
	
	protected AbstractCommand(MessageFormatter formatter)
	{
		m_fullName = formatter.format(this.getClass().getName() + ".name"); 
		m_nameParts = NameUtils.splitName(m_fullName);
	}
	
	/**
	 * Returns the full, human readable name of a command
	 */
	public String getFullName()
	{
		return m_fullName;
	}
	
	public String[] getNameParts()
	{
		return m_nameParts;
	}

	/**
	 * Returns <tt>true</tt> if the supplied command matches
	 * this command.
	 * @param commandParts The normalized command parts
	 */
	public int match(String[] commandParts)
	{
		int myTop = m_nameParts.length;
		int itsTop = commandParts.length;
		
		// Spurious parameters aren't allowed
		//
		if(!this.acceptsParameters() && itsTop  > myTop)
			return 0;
			
		// Start matching
		//
		int top = Math.min(myTop, itsTop);
		for(int idx = 0; idx < top; ++idx)
		{
			String candidate = commandParts[idx];
			String myPart = m_nameParts[idx];
			if(candidate.length() > myPart.length())
				return 0; 
			if(!candidate.equals(myPart.substring(0, candidate.length())))
				return 0;
		}
		// If we got this far, we have a match! Whee!!
		//
		return top;
	}

	public boolean acceptsParameters()
	{
		return false;
	}
}
