/*
 * Created on Sep 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceListItem;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListConferencesAlphabetically extends AbstractCommand
{

	public ListConferencesAlphabetically(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
		PrintWriter out = context.getOut();
		ConferenceListItem[] names = context.getSession().listConferencesByName();
		int top = names.length;
		if(top == 0)
		    return;
		MessageFormatter formatter = context.getMessageFormatter();
		HeaderPrinter hp = new HeaderPrinter();
		hp.addHeader(formatter.format("list.conferences.last.active"), 17, false);
		hp.addSpace(1);
		hp.addHeader(formatter.format("list.conferences.name"), 50, false);
		hp.printOn(out);
		for(int idx = 0; idx < top; ++idx)
		{
		    ConferenceListItem item = names[idx]; 
		    PrintUtils.printLeftJustified(out, context.smartFormatDate(item.getLastActive()), 17);
		    out.print(' ');
		    out.println(item.getName().getName());
		}
	}
}

