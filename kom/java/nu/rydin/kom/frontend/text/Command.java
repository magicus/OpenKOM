/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface Command
{
	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException;
	
	public String getFullName();
	
	public String[] getNameParts();
	
	/**
	 * If the command matched, returns the number of matching parts (words).
	 * Otherwise, return 0.
	 * 
	 * @param command The command parts
	 * @return The number of matching parts
	 */
	public int match(String[] command);

	public boolean acceptsParameters();
	
	public boolean expectsNumericParameter();

	public String[] getParameters(String[] parts);
	
	/**
	 * Prints characters preceeding command output, typically 
	 * a newline.
	 * @param out The stream to print on
	 */
	public void printPreamble(PrintWriter out);
	
	/**
	 * Prints characters succeeding command output, typically
	 * a newline.
	 * @param out The stream to print on
	 */
	public void printPostamble(PrintWriter out);

	/**
	 * @return
	 */
	public CommandLineParameter[] getSignature();

	/**
	 * @param context
	 * @param parameterArray
	 */
	public void execute2(Context context, Object[] parameterArray);

}
