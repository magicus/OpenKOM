/*
 * Created on Sep 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.SystemFiles;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.NonWrappingWrapper;
import nu.rydin.kom.frontend.text.editor.simple.FileEditor;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeWelcomeMessage extends AbstractCommand
{
    public ChangeWelcomeMessage(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);	
	}
    
    public void checkAccess(Context context) throws AuthorizationException
    {
        context.getSession().checkRights(UserPermissions.ADMIN);
    }
    
    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();
        session.checkRights(UserPermissions.ADMIN);
        FileEditor editor = new FileEditor(context);
        MessageFormatter formatter = context.getMessageFormatter();
        PrintWriter out = context.getOut();
        try
        {
            String content = session.readSystemFile(SystemFiles.WELCOME_MESSAGE);
            editor.fill(new NonWrappingWrapper(content));
        } 
        catch (ObjectNotFoundException e)
        {
            // Not found
            //
            out.println();
            out.println(formatter.format("edit.file.new.file"));
            out.println();
        }
        String newContent = editor.edit().getBody();

        // Store in file
        //
        session.storeSystemFile(SystemFiles.WELCOME_MESSAGE, newContent);

    }

}
