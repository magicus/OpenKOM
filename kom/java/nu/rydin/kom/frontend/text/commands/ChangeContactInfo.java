/*
 * Created on Oct 1, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.UserPermissions;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.UserParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UserInfo;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ChangeContactInfo extends AbstractCommand
{

    public ChangeContactInfo(Context context, String fullName)
	{
		super(fullName, new CommandLineParameter[] { new UserParameter(false) });	
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        // We must hold the USER_ADMIN priv to change the info for others
        // than ourselves.
        //
        ServerSession session = context.getSession();
        long id = parameters[0] != null
        	? ((NameAssociation) parameters[0]).getId()
        	: session.getLoggedInUserId();
        if(id != context.getLoggedInUserId())
            session.checkRights(UserPermissions.USER_ADMIN);
                
        // Load existing data
        //
        UserInfo ui = session.getUser(id);
        
        // Print info about who we're changing 
        //
        MessageFormatter formatter = context.getMessageFormatter();
        PrintWriter out = context.getOut();
        out.println(formatter.format("change.contact.info.prompt", ui.getName()));
        out.println();

        // Allow user to change data
        //
        ui.setAddress1(this.getLine(context, "change.contact.info.address1", ui.getAddress1())); 
        ui.setAddress2(this.getLine(context, "change.contact.info.address2", ui.getAddress2()));
        ui.setAddress3(this.getLine(context, "change.contact.info.address3", ui.getAddress3()));
        ui.setAddress4(this.getLine(context, "change.contact.info.address4", ui.getAddress4()));
        ui.setPhoneno1(this.getLine(context, "change.contact.info.phoneno1", ui.getPhoneno1()));
        ui.setPhoneno2(this.getLine(context, "change.contact.info.phoneno2", ui.getPhoneno2()));
        ui.setEmail1(this.getLine(context, "change.contact.info.email1", ui.getEmail1()));
        ui.setEmail2(this.getLine(context, "change.contact.info.email2", ui.getEmail2()));
        ui.setUrl(this.getLine(context, "change.contact.info.url", ui.getUrl()));
        
        // Do it!
        //
        session.changeContactInfo(ui);
    }
    
    private String getLine(Context ctx, String promptKey, String defaultValue)
    throws IOException, InterruptedException, OperationInterruptedException
    {
        ctx.getOut().print(ctx.getMessageFormatter().format(promptKey));
        ctx.getOut().flush();
        return ctx.getIn().readLine(defaultValue);
    }
}
