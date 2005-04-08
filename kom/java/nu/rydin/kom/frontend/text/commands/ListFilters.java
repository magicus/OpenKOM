/*
 * Created on Oct 20, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.Relationship;
import nu.rydin.kom.utils.FlagUtils;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Pontus Rydin
 */
public class ListFilters extends AbstractCommand
{
	public ListFilters(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions); 
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        DisplayController dc = context.getDisplayController();
        ServerSession session = context.getSession();
        Relationship[] jinglar = session.listFilters();
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        HeaderPrinter hp = new HeaderPrinter();
        dc.normal();
        hp.addHeader(formatter.format("list.filters.user"), 38, false);
        hp.addSpace(1);
        hp.addHeader(formatter.format("list.filters.flags"), 38, false);
        hp.printOn(out);
        for (int idx = 0; idx < jinglar.length; idx++)
        {
            Relationship r = jinglar[idx];
            dc.output();
            PrintUtils.printLeftJustified(out, session.getName(r.getReferee()).toString(), 38);
            out.print(' ');
            dc.normal();
            FlagUtils.printFlagsShort(out, formatter, context.getFlagLabels("filterflags"), 
                    new long[] { r.getFlags() } );
            out.println();
        }
    }
}
