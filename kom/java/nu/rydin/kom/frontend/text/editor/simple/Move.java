/*
 * Created on Sep 3, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;

import nu.rydin.kom.constants.ConferencePermissions;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Move extends AbstractCommand
{
    public Move(Context context, String fullName)
    {
        super(fullName, new CommandLineParameter[] { new ConferenceParameter("simple.editor.receiver", true) });
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        NameAssociation recipient = (NameAssociation) parameters[0];
        context.getSession().assertConferencePermission(recipient.getId(),
                ((EditorContext) context).getReplyTo() != -1
                	? ConferencePermissions.REPLY_PERMISSION
                	: ConferencePermissions.WRITE_PERMISSION);
        ((EditorContext) context).setRecipient(recipient);
    }
}
