/*
 * Created on Jun 7, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class ListMembers extends AbstractCommand 
{
	public ListMembers(String fullName) 
	{
		super(fullName);
	}

	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		long confid = 	0 == parameters.length ?
					  	context.getSession().getCurrentConference().getId() :
					  	NamePicker.resolveName(NameUtils.assembleName(parameters), (short) -1, context);
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

	public boolean acceptsParameters()
	{
		return true;
	}
}
