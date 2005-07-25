/*
 * Created on Apr 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Pontus Rydin
 */
public class EnableNormalPrivs extends AbstractCommand
{
    public EnableNormalPrivs(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new UserParameter(true) }, permissions);
    }
    
    public void execute(Context context, Object[] parameterArray)
    throws KOMException, IOException, InterruptedException
    {
        PrintWriter out = context.getOut();
        MessageFormatter formatter = context.getMessageFormatter();
        NameAssociation user = (NameAssociation) parameterArray[0]; 
        context.getSession().changeUserPermissions(user.getId(), UserPermissions.NORMAL, ~UserPermissions.NORMAL);
        out.println(formatter.format("enable.normal.privs.confirmation", user.getName().getName()));
        
		// Clear cache
		//
		context.clearUserInfoCache();
    }
}
