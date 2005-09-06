/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.CommandLinePart;
import nu.rydin.kom.frontend.text.parser.CommandNamePart;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public interface Command
{
    /**
     * Executes the command with the given parameters in the given context.
     * @param context
     * @param parameters
     * @throws KOMException
     * @throws IOException
     * @throws InterruptedException
     */
	public void execute(Context context, Object[] parameters)
	throws KOMException, IOException, InterruptedException;
	
	/**
	 * Returns the full, human readable, name of this instance of a Command.
	 * @return
	 */
	public String getFullName();

	/**
	 * Checks to see is the current user has access to execute this command.
	 * Used by the parser to abort execution before parameter completion.
	 * @param context
	 * @throws AuthorizationException
	 * @throws UnexpectedException
	 */
	public void checkAccess(Context context) throws AuthorizationException, UnexpectedException;
	
	/** 
	 * Returns <tt>true</tt> if the current user is allowed to execute this command
	 * @param context
	 * @throws UnexpectedException
	 */
	public boolean hasAccess(Context context) throws UnexpectedException;
	
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
	 * Gets the part of the signature containing the parameters.
	 * @return
	 */
	public CommandLineParameter[] getSignature();
	
	/**
	 * Gets the part of the sugnature containing the nameparts.
	 * @return
	 */
	public CommandNamePart[] getNameSignature();
	
	/**
	 * Gets the full, combined signature.
	 * @return
	 */
	public CommandLinePart[] getFullSignature();
	
	/**
	 * Returns a bit pattern of the permissions required to run this command
	 * @return
	 */
	public long getRequiredPermissions();
}
