/*
 * Created on Jun 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.AuthorizationException;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.MissingArgumentException;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class RenameObject extends AbstractCommand
{
	public RenameObject(String fullName)
	{
		super(fullName);
	}
	
	public void execute(Context context, String[] parameters)
		throws KOMException, IOException, InterruptedException
	{
		// Handle parameters
		//
		if(parameters.length == 0)
			throw new MissingArgumentException();

		long id = NamePicker.resolveName(NameUtils.assembleName(parameters), (short) -1, context);
		
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
		
		String oldName = session.getName(id);
		
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
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
