/*
 * Created on Jun 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class RenameObject extends AbstractCommand
{
	public RenameObject(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new NamedObjectParameter(true) });
	}
	
	public void execute(Context context, Object[] parameterArray)
		throws KOMException, IOException, InterruptedException
	{
		long id = ((NameAssociation)parameterArray[0]).getId();
		
		// Set up
		//
		LineEditor in = context.getIn();
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		ServerSession session = context.getSession();
		
		// Are we allowed to rename this object?
		//
		if(!session.userCanChangeNameOf(id))
			throw new AuthorizationException();
		
		String oldName = session.getName(id).getName();
		
		// Print prompt
		//
		out.print(formatter.format("rename.prompt", oldName));
		out.flush();
		
		// Read new name
		//
		String newName = in.readLine();
		
		// Empty name? User interrupted
		//
		if(newName.length() == 0)
			return;

		
		// Execute
		//
		session.renameObject(id, newName);
		
		// Print confirmation
		//
		out.println(formatter.format("rename.confirmation", new Object [] { 
		        context.formatObjectName(oldName, id), context.formatObjectName(newName, id) }));
	}
}
