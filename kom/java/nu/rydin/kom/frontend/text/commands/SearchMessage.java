/*
 * Created on Jul 15, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Henrik Schröder
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class SearchMessage extends AbstractCommand 
{
    private static final int CHUNK_SIZE = 50;
	public SearchMessage(Context context, String fullName) 
	{
		super(fullName, new CommandLineParameter[] { new RawParameter("search.param.0.ask", true) });
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
		KOMWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter mf = context.getMessageFormatter();

		//chop chop last space
		String searchterm = ((String)parameterArray[0]).trim();
				
		// TODO: Get out of here if there are no messages
		//
				
		// Print headers and a blank line.
		//
		PrintUtils.printRightJustified(out, mf.format("search.heading.text"), 7);
		out.print("  ");
		PrintUtils.printLeftJustified(out, mf.format("search.heading.writtenby"), 30);
		out.print("  ");
		PrintUtils.printLeftJustified(out, mf.format("search.heading.subject"), 30);
		out.println();
		out.println();
		out.flush();
		
		for(int offset = 0;; offset += CHUNK_SIZE)
		{
		    MessageSearchResult[] msr = context.getSession().searchMessagesInConference(context.getSession().getCurrentConferenceId(), searchterm, offset, CHUNK_SIZE);
		    int top = msr.length;
		    if(top == 0)
		        break;
			for (int i = 0; i < top; ++i)
			{
				
				PrintUtils.printRightJustified(out, String.valueOf(msr[i].getId()), 7);
				out.print("  "); /* Inject spaces to separate localnum from author*/
				PrintUtils.printLeftJustified(out, context.formatObjectName(msr[i].getUsername(), msr[i].getUser()), 30);
				out.print("  ");
				PrintUtils.printLeftJustified(out, msr[i].getSubject(), 30);
				out.println();
				out.flush();
			}
		}
	}
}
