/*
 * Created on Jun 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
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
		int screenHeight = 25;	// TODO: Pull this from user term config.
		int toprow = 0;			// Initial value: Start from top

		KOMPrinter out = context.getKOMPrinter();
		LineEditor in = context.getIn();
		MessageFormatter mf = context.getMessageFormatter();
		
		// Retrieve the first batch, if it exists
		// Note: Not having written any texts whatsoever is such a fucking 
		// edge-case I won't bother with it, and those user are losers anyway.
		// SCREW THEM!!!
		LocalMessageHeader[] lmh = context.getSession().listGlobalMessagesByUser(context.getLoggedInUserId(), toprow, screenHeight);
		
		// Getting the more prompt
		//
		StringBuffer sb = new StringBuffer();
		sb.append(mf.format("misc.more"));
		sb.append(" (");
		sb.append(mf.format("misc.y"));
		sb.append("/");
		sb.append(mf.format("misc.n"));
		sb.append(") ");
		String question = sb.toString();
		sb = null;
		
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

		// Main rollercoaster
		//
		while (true)
		{
		
			for (int i = 0; i < lmh.length; ++i)
			{
				//Naah, fuck the global message number
				//PrintUtils.printRightJustified(out.toPrintWriter(), "(" + lmh[i].getId() + ")", 7);
				PrintUtils.printRightJustified(out.toPrintWriter(), String.valueOf(lmh[i].getLocalnum()), 7);
				out.print("  ");
				//Personal mailfolder prints as username instead of "Brevlåda". WTF?
				PrintUtils.printLeftJustified(out.toPrintWriter(), context.getSession().getName(lmh[i].getConference()), 30);
				out.print("  ");
				PrintUtils.printLeftJustified(out.toPrintWriter(), lmh[i].getSubject(), 30);
				out.println();
				out.flush();
			}
			
			if (lmh.length == screenHeight)
			{
				//Pre-fetch next page to see if we should print more-prompt.
				//Avoiding ful-kod! Sweet!
				//
				lmh = context.getSession().listGlobalMessagesByUser(context.getLoggedInUserId(), toprow+screenHeight, screenHeight);
				if (lmh.length > 0)
				{
					//Yes, next page had more stuff in it. Go me!
					//
					if (!(in.getYesNo(question, mf.format("misc.more.yeschars").toCharArray(), mf.format("misc.more.nochars").toCharArray())))
					{
						break; // We're done!
					}
					else
					{
						toprow += screenHeight;
						continue;
					}
				}
				else
				{
					break; // All messages displayed, we're so fucking outta here.
				}
			}
			else
			{
				break; // All messages displayed, we're outta here.
			}
		}
	}
}
