/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.CommandLinePart;
import nu.rydin.kom.frontend.text.parser.CommandNamePart;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class AbstractCommand implements Command
{
    public final static CommandLineParameter[] NO_PARAMETERS = new CommandLineParameter[0];
	
    private String m_fullName;
	
	private CommandLineParameter[] m_signature;
    private CommandNamePart[] m_nameSignature;
    private CommandLinePart[] m_fullSignature;
	
	protected AbstractCommand(String fullName, CommandLineParameter[] signature)
	{
		m_fullName = fullName;
		m_signature = signature;
		
		m_nameSignature = splitName(fullName);
		
		m_fullSignature = new CommandLinePart[m_nameSignature.length + m_signature.length];
		System.arraycopy(m_nameSignature, 0, m_fullSignature, 0, m_nameSignature.length);
		System.arraycopy(m_signature, 0, m_fullSignature, m_nameSignature.length, m_signature.length);
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
    public CommandLinePart[] getFullSignature()
    {
        return m_fullSignature;
    }
    public CommandNamePart[] getNameSignature()
    {
        return m_nameSignature;
    }
    
	private CommandNamePart[] splitName(String name)
	{
		String cooked = CommandLinePart.cookString(name);
		String[] cookedParts = cooked.split(" ");
		CommandNamePart[] result = new CommandNamePart[cookedParts.length];
		for (int i = 0; i < cookedParts.length; i++)
		{
			String cookedPart = cookedParts[i];
			result[i] = new CommandNamePart(cookedPart, true);
		}
		return result;
	}
    
}
