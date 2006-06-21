/*
 * Created on Oct 15, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 */
public class WriteMessage extends AbstractCommand
{
    public WriteMessage(Context context, String fullName, long permissions)
    {
        super(fullName, AbstractCommand.NO_PARAMETERS, permissions);
    }

    public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();
        
        // Check permissions so we can bail out earlier
        //
        session.assertConferencePermission(session.getCurrentConferenceId(), ConferencePermissions.WRITE_PERMISSION);

        // Get editor and execute it
        //
        MessageEditor editor = context.getMessageEditor();
        ConferenceInfo recipient = session.getCurrentConference();
        editor.setRecipient(new NameAssociation(recipient.getId(), recipient.getName()));
        UnstoredMessage msg = editor.edit();

        // Store text
        //
        MessageOccurrence occ = session.storeMessage(editor.getRecipient()
                .getId(), msg);
        context.getOut().println();
        context.getOut().println(
                context.getMessageFormatter().format("write.message.saved",
                        new Object[] { new Integer(occ.getLocalnum()), new Long(occ.getGlobalId())} ));
    }
}