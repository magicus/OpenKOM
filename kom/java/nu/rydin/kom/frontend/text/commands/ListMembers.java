/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class ListMembers extends AbstractCommand 
{
	public ListMembers(Context context, String fullName) 
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(false)});
	}

	public void execute(Context context, Object[] parameterArray)
	throws KOMException 
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
	    long confid;
	    if (nameAssociation == null) {
	        confid = context.getSession().getCurrentConference().getId();
	    } else {
	        confid = nameAssociation.getId();
	    }
		String[] mbrs = context.getSession().listMemberNamesByConference(confid);
					  	
		PrintWriter out = context.getOut();
		MessageFormatter fmt = context.getMessageFormatter();
		if (0 == mbrs.length)
		{
			out.println(fmt.format("list.members.empty"));
			out.flush();
		}
		else
		{
			out.println(fmt.format("list.members.begin", context.getSession().getName(confid)));
			out.println();
			for (int i = 0; i < mbrs.length; ++i)
			{
				out.println(mbrs[i]);
			}
		}
	}
}
