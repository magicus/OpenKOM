/*
 * Created on Oct 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.EllipsisParameter;
import nu.rydin.kom.frontend.text.parser.FlagParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Pontus Rydin
 */
public class CreateFilter extends AbstractCommand
{
	public CreateFilter(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] 
		   { new UserParameter(true), 
		        new EllipsisParameter("create.filter.type.ask", true, new FlagParameter(true, 
		                context.getFlagLabels("filterflags")))});	
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        NameAssociation jinge = (NameAssociation) parameters[0];
        Object[] flagList = (Object[]) parameters[1];
        long flags = 0;
        for (int i = 0; i < flagList.length; i++)
            flags |= 1 << ((Integer) flagList[i]).intValue();
        context.getSession().createUserFilter(jinge.getId(), flags);
        context.getOut().println(context.getMessageFormatter().
                format("create.filter.confirmation", jinge.getName().toString()));
    }
}
