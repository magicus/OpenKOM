/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.parser.CommandLineParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class AbstractCommand implements Command
{
    public final static CommandLineParameter[] NO_PARAMETERS = new CommandLineParameter[0];
	private String m_fullName;
	private CommandLineParameter[] m_signature;
	
	protected AbstractCommand(String fullName, CommandLineParameter[] signature)
	{
		m_fullName = fullName;
		m_signature = signature;
	}
	
	/**
	 * Returns the full, human readable name of a command
	 */
	public String getFullName()
	{
		return m_fullName;
	}
	
	public void printPreamble(PrintWriter out)
	{
		out.println();
	}
	
	public void printPostamble(PrintWriter out)
	{
		out.println();
	}	

	public String toString() 
	{
        return getClass().getName() + "[" + m_fullName + "]";
    }
    
    public CommandLineParameter[] getSignature() 
    {
        return m_signature;
    }
}
