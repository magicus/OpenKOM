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
	public void execute2(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException;
	
	public String getFullName();
	
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

}
