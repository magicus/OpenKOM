/*
 * Created on Jun 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.MessageOccurrence;

/**
 * @author Henrik Schröder
 *
 */
public class ListOwnMessages extends AbstractCommand 
{
	public ListOwnMessages(String fullName) 
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
	    //TODO (skrolle) Fix this shit, add subject!
		MessageOccurrence[] mh = context.getSession().listGlobalMessagesByUser(context.getLoggedInUserId());
		PrintWriter out = context.getOut();
		out.println("Skrolle Är En Liten Apa!");
	}
}
