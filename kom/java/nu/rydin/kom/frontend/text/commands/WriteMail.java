/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.Activities;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class WriteMail extends AbstractCommand
{
    public WriteMail(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new NamedObjectParameter(true) }, permissions);
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {
        ServerSession ss = context.getSession();
        
        // Get parameters
        //
        NameAssociation recipient = (NameAssociation) parameterArray[0];

        // Update state
        //
        ss.setActivity(Activities.MAIL, true);
        ss.setLastObject(recipient.getId());
        
        try
        {
            // Has the user left a note? If so, print it. 
            //
            PrintWriter out = context.getOut();
            String note = context.getSession().readFile(recipient.getId(), ".note.txt");
            String[] lines = note.split("\n");

            MessageFormatter mf = context.getMessageFormatter();
            out.println(mf.format("status.user.note"));
            for (int i = 0; i < lines.length; ++i)
            {
                out.println(lines[i]);
            }
            out.println();
        }
        catch (ObjectNotFoundException e)
        {
            // No note, so nothing to print
        }
        catch (AuthorizationException e)
        {
            // This shouldn't even happen in a user situation. Even IF it happens, we
            // still have nothing to print and so fail silently.
        }
        
        // Get editor and execute it
        //
        MessageEditor editor = context.getMessageEditor();
        UnstoredMessage msg;
        try
        {
            editor.setRecipient(recipient);
            msg = editor.edit();
        }
        finally
        {
            context.getSession().restoreState();
        }
        @SuppressWarnings("unused")
        MessageOccurrence occ = context.getSession().storeMail(
                editor.getRecipient().getId(), msg);

        context.getOut().println(
                context.getMessageFormatter().format("write.mail.saved", context.formatObjectName(context.getSession().getName(recipient.getId()), recipient.getId())));
    }
}