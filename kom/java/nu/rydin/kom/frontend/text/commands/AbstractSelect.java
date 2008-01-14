/*
 * Created on Jan 13, 2008
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
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class AbstractSelect extends AbstractCommand
{  
    public AbstractSelect(String fullName, CommandLineParameter[] signature,
            long permissions)
    {
        super(fullName, signature, permissions);
    }

    protected abstract boolean select(Context context, Object[] parameters)
    throws KOMException;

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        boolean complete = this.select(context, parameters);
        KOMWriter w = context.getOut();
        MessageFormatter fmt = context.getMessageFormatter();
        ServerSession ss = context.getSession();
        w.println(fmt.format("search.count", ss.getSelectedMessages().getMessages().length));
        if(!complete)
        {
            w.println();
            w.println(context.getMessageFormatter().format("select.overflow"));
        }
    }
}
