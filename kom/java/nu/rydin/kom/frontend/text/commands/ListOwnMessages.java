/*
 * Created on Jun 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.data.ConferenceManager;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMPrinter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.LocalMessageHeader;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Henrik Schröder
 *
 */
public class ListOwnMessages extends AbstractCommand 
{
	public ListOwnMessages(String fullName) 
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		LocalMessageHeader[] lmh = context.getSession().listGlobalMessagesByUser(context.getLoggedInUserId());
		KOMPrinter out = context.getKOMPrinter();
		MessageFormatter mf = context.getMessageFormatter();
		
		// Print headers and a blank line.
		//
		PrintUtils.printRightJustified(out.toPrintWriter(), mf.format("list.own.heading.text"), 7);
		out.print("  ");
		PrintUtils.printLeftJustified(out.toPrintWriter(), mf.format("list.own.heading.conference"), 30);
		out.print("  ");
		PrintUtils.printLeftJustified(out.toPrintWriter(), mf.format("list.heading.subject"), 30);
		out.println();
		out.println();
		out.flush();

		//TODO: Add paging of data in a nice, general way.
		for (int i = 0; i < lmh.length; ++i)
		{
			//Naah, fuck the global message number
			//PrintUtils.printRightJustified(out.toPrintWriter(), "(" + lmh[i].getId() + ")", 7);
			PrintUtils.printRightJustified(out.toPrintWriter(), String.valueOf(lmh[i].getLocalnum()), 7);
			out.print("  ");
			// TODO (skrolle) Or should we use getSession().getName() instead? 
			PrintUtils.printLeftJustified(out.toPrintWriter(), context.getSession().getConference(lmh[i].getConference()).getName(), 30);
			out.print("  ");
			PrintUtils.printLeftJustified(out.toPrintWriter(), lmh[i].getSubject(), 30);
			out.println();
			out.flush();
		}
		
		LineEditor in = context.getIn();
		if (in.getYesNo("Tut i luren? abc/def", "\r".toCharArray(), " ".toCharArray()))
		{
			out.println("TJOOHOO!");
		}
		else
		{
			out.println("NEEEEEEEEEEJ");
		}
	}
}
