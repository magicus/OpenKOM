/*
 * Created on Jun 9, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author Jepson
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */

public class ListTexts extends AbstractCommand 
{
    private static final int CHUNK_SIZE = 50;
    
	public ListTexts(String fullName) 
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException 
	{		
		MessageFormatter mf = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		
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
		ServerSession session = context.getSession();

		for(int offset = 0;; offset += CHUNK_SIZE)
		{
		    MessageHeader[] mh = session.listMessagesInCurrentConference(offset, CHUNK_SIZE);
		    int top = mh.length;
		    if(top == 0)
		        break;
			for (int i = 0; i < top; ++i)
			{
				PrintUtils.printRightJustified(out, String.valueOf(mh[i].getId()), 7);
				out.print("  "); /* Inject spaces to separate localnum from author*/
				PrintUtils.printLeftJustified(out, context.formatObjectName(mh[i].getAuthorName(), mh[i].getAuthor()), 30);
				out.print("  ");
				PrintUtils.printLeftJustified(out, mh[i].getSubject(), 30);
				out.println();
				out.flush();
			}
		}
	}
}
