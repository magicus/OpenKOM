/*
 * Created on Aug 29, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.NonWrappingWrapper;
import nu.rydin.kom.frontend.text.editor.simple.FileEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeLoginScript extends AbstractCommand
{
	public ChangeLoginScript(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);	
	}

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        
        // Try to load file
        //
        String fileName = ".login.cmd";
        long parent = context.getLoggedInUserId();
        FileEditor editor = new FileEditor(context);
        try
        {
            String content = session.readFile(parent, fileName);
            editor.fill(new NonWrappingWrapper(content));
        }
        catch(ObjectNotFoundException e)
        {
            // Not found
            //
            out.println();
            out.println(formatter.format("edit.file.new.file"));
            out.println();
        }
        String newContent = editor.edit(-1).getBody();
        
        // Store in file
        //
        session.storeFile(parent, fileName, newContent);

    }
}
