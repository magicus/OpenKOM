/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.utils.StringUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class AbstractCommand implements Command
{
	protected String m_fullName;
	protected String[] m_nameParts;
	
	protected AbstractCommand(String fullName)
	{
		m_fullName = fullName;  
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
		
		// If we accept parameters and we expect them to be numeric,
		// then don't consider this a match for nun-numeric paramters.
		//
		if(this.acceptsParameters() && this.expectsNumericParameter() &&
			!StringUtils.isMessageNumber(commandParts[myTop]))
		    return 0;

		
		// If we got this far, we have a match! Whee!!
		//
		return top;
	}

	public boolean acceptsParameters()
	{
		return false;
	}
	
	public boolean expectsNumericParameter()
	{
	    return false;
	}
	
	public String[] getParameters(String[] parts)
	{	
		int top = this.getNameParts().length;
		int numParts = parts.length;
		String[] args = null;
		if(numParts <= top)
			args = new String[0];
		else
		{
			int numArgs = numParts - top;
			args = new String[numArgs];
			System.arraycopy(parts, top, args, 0, numArgs);
		}		
		return args;
	}

	public void printPreamble(PrintWriter out)
	{
		out.println();
	}
	
	public void printPostamble(PrintWriter out)
	{
		out.println();
	}	
	public CommandLineParameter[] getSignature()
	{
		// TODO Auto-generated method stub
		return new CommandLineParameter[0];
	}
	public void execute2(Context context, Object[] parameterArray)
	{
	// TODO Auto-generated method stub

	}
}
