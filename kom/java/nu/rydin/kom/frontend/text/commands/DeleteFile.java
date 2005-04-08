/*
 * Created on Aug 27, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.FilenameParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 */
public class DeleteFile extends AbstractCommand
{
    public DeleteFile(Context context, String fullname, long permissions)
    {
        super(fullname, new CommandLineParameter[]
            { new FilenameParameter(true), new NamedObjectParameter(false) }, permissions);
    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();

        // Extract data from parameters
        //
        String fileName = (String) parameters[0];
        long parent = parameters[1] != null ? ((NameAssociation) parameters[1])
                .getId() : context.getSession().getCurrentConferenceId();

        // Delete it!
        //
        session.deleteFile(parent, fileName);
    }
}