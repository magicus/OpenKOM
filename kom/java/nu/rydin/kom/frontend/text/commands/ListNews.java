/*
 * Created on Nov 9, 2003
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
import nu.rydin.kom.structs.MembershipListItem;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListNews extends AbstractCommand
{
	public ListNews(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

	public void execute(Context context, Object[] parameterArray)
		throws KOMException
	{
		PrintWriter out = context.getOut();
		MessageFormatter formatter = context.getMessageFormatter();
		MembershipListItem[] list = context.getSession().listNews();
		int total = 0;
		int top = list.length;
		for(int idx = 0; idx < top; ++idx)
		{
			MembershipListItem each = list[idx];
			out.println(formatter.format("list.news.item", new Object[] 
				{ new Integer(each.getUnread()), 
			        context.formatObjectName(each.getConference().getName(), each.getConference().getId()) }));
			total += each.getUnread();
		}
		if(total > 0)
		{
		    out.println();
			out.println(formatter.format("list.news.total", new Integer(total)));
		}
		else
		    out.println(formatter.format("list.news.no.news"));
	}
}
