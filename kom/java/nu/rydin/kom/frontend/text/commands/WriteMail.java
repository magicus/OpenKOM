/*
 * Created on Nov 10, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
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
        // Get parameters
        //
        NameAssociation recipient = (NameAssociation) parameterArray[0];

        // Get editor and execute it
        //
        MessageEditor editor = context.getMessageEditor();
        editor.setRecipient(recipient);
        UnstoredMessage msg = editor.edit();
        MessageOccurrence occ = context.getSession().storeMail(
                editor.getRecipient().getId(), msg);

        context.getOut().println(
                context.getMessageFormatter().format("write.mail.saved", context.formatObjectName(context.getSession().getName(recipient.getId()), recipient.getId())));
    }
}