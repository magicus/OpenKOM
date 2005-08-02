/*
 * Created on Aug 25, 2004
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
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.editor.NonWrappingWrapper;
import nu.rydin.kom.frontend.text.editor.simple.FileEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.FilenameParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 */
public class EditFile extends AbstractCommand
{

    public EditFile(Context context, String fullname, long permissions)
    {
        super(fullname, new CommandLineParameter[] {
                new FilenameParameter(true), new NamedObjectParameter(false) }, permissions);
    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();
        DisplayController dc = context.getDisplayController();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();

        // Extract data from parameters
        //
        String fileName = (String) parameters[0];
        long parent = parameters[1] != null ? ((NameAssociation) parameters[1])
                .getId() : context.getSession().getCurrentConferenceId();

        //FIXME EDITREFACTOR: No file editor displaycontroller controls are present.
        dc.normal();
                
        // TODO: Check for write permission!!!
        //
        // Try to load file
        //
        FileEditor editor = new FileEditor(context);
        try
        {
            String content = session.readFile(parent, fileName);
            editor.fill(new NonWrappingWrapper(content));
        } 
        catch (ObjectNotFoundException e)
        {
            // Not found
            //
            out.println(formatter.format("edit.file.new.file"));
            out.println();
        }
        
		out.println(formatter.format("edit.file.header", fileName));
        //FIXME EDITREFACTOR: When editing an existing file the previous contents are not displayed.
        String newContent = editor.edit().getBody();

        // Store in file
        //
        session.storeFile(parent, fileName, newContent, 0);
    }
}