/*
 * Created on Jun 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuffer;

import nu.rydin.kom.frontend.text.LineEditor; 
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.KOMException;
import nu.rydin.kom.EmptyConferenceException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.utils.PrintUtils;
import nu.rydin.kom.structs.MessageHeader;

/**
 * @author Jepson
 */

public class ListTexts extends AbstractCommand 
{
	public ListTexts(String fullName) 
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		int screenHeight = 25;	// TODO: Pull this from user term config.
		int toprow = 0;			// Initial value: Start from top
		
		// Retrieve the first batch, if it exists
		//
		MessageHeader[] mh = context.getSession().listMessagesInCurrentConference(toprow, screenHeight);
		if (0 == mh.length)
		{
			throw new EmptyConferenceException();
		}
		MessageFormatter mf = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();

		// This prompt is used to ask the user if we should display another page.
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
		PrintUtils.printRightJustified(out, mf.format("list.heading.text"), 7);
		out.print("  ");
		PrintUtils.printLeftJustified(out, mf.format("list.heading.writtenby"), 30);
		out.print("  ");
		PrintUtils.printLeftJustified(out, mf.format("list.heading.subject"), 30);
		out.println();
		out.println();
		out.flush();
		
		// Mainlupe
		//
		do
		{
			// Dump what we retrieved
			//
			for (int i = 0; i < mh.length; ++i)
			{
				PrintUtils.printRightJustified(out, String.valueOf(mh[i].getId()), 7);
				out.print("  "); /* Inject spaces to separate localnum from author*/
				PrintUtils.printLeftJustified(out, mh[i].getAuthorName(), 30);
				out.print("  ");
				PrintUtils.printLeftJustified(out, mh[i].getSubject(), 30);
				out.println();
				out.flush();
			}
			
			// Fulkod. This can give a false positive, if the number of messages in the conference
			// is a multiple of screenHeight. We should retrieve the message count before entering
			// the main loop.
			//
			if (mh.length == screenHeight)
			{
				int choice = in.getChoice(question, 
										  new String[] { mf.format("misc.y"), 
														 mf.format("misc.n") }, 
										  0, 
										  mf.format("parser.invalid.choice"));
				if (0 != choice)
				{
					break; // We're done!
				}
				else
				{
					toprow += screenHeight;
					mh = context.getSession().listMessagesInCurrentConference(toprow, screenHeight);
					continue;
				}
			}
			else
			{
				break; // All messages displayed, we're outta here.
			}
		}
		while (true);
	}
	
	public boolean acceptsParameters()
	{
		return true;
	}
}
