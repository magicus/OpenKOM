/*
 * Created on Oct 11, 2003
 *  
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceListItem;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListConferences extends AbstractCommand
{
	public ListConferences(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

	public void execute(Context context, Object[] parameterArray) 
	throws KOMException
	{
		printConferenceList(context, context.getSession().listConferencesByDate());
	}
	
	protected static void printConferenceList(Context context, ConferenceListItem[] list)
	throws UnexpectedException
	{
		PrintWriter out = context.getOut();
		int top = list.length;
		if(top == 0)
		    return;
		DisplayController dc = context.getDisplayController();
		dc.normal();
		MessageFormatter formatter = context.getMessageFormatter();
		HeaderPrinter hp = new HeaderPrinter();
		hp.addHeader(formatter.format("list.conferences.last.active"), 17, false);
		hp.addSpace(4);
		hp.addHeader(formatter.format("list.conferences.name"), 50, false);
		hp.printOn(out);
		for(int idx = 0; idx < top; ++idx)
		{
		    ConferenceListItem item = list[idx]; 
		    dc.normal();
		    PrintUtils.printLeftJustified(out, context.smartFormatDate(item.getLastActive()), 17);
		    dc.highlight();
		    out.print(' ');
		    out.print(item.isMember() ? ' ' : '*');
		    out.print(item.isOwner() ? 'A' : ' ');
		    out.print(' ');
		    dc.output();
		    out.println(item.getName().getName());
		}
		dc.normal();	    
	}
}
