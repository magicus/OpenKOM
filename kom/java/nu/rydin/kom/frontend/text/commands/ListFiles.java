/*
 * Created on Aug 27, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.constants.FileProtection;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.NoFilesException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.FileStatus;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ListFiles extends AbstractCommand
{
	public ListFiles(Context context, String fullname, long permissions)
	{
		super(fullname, new CommandLineParameter[] {  new NamedObjectParameter(false)}, permissions);
	}

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();
        DisplayController dc = context.getDisplayController();
        
        // Resolve parent
        //
        long parent = parameters[0] != null
    	? ((NameAssociation) parameters[0]).getId()
    	        : context.getSession().getCurrentConferenceId();
    	
    	// Ask server for file list
    	//
        FileStatus[] files = session.listFiles(parent, "%");
        int top = files.length;
        if(top == 0)
            throw new NoFilesException();
        MessageFormatter formatter = context.getMessageFormatter();
        HeaderPrinter hp = new HeaderPrinter();
        PrintWriter out = context.getOut();
        
        // Print header
        //
        dc.normal();
        hp.addHeader(formatter.format("list.files.created"), 18, false);
        hp.addHeader(formatter.format("list.files.updated"), 18, false);
        hp.addHeader(formatter.format("list.files.permissions"), 12, false);
        hp.addSpace(1);
        hp.addHeader(formatter.format("list.files.name"), 10, false);
        hp.printOn(out);
        
        // Print file list
        //
        for(int idx = 0; idx < top; ++idx)
        {
            dc.normal();
            FileStatus each = files[idx];
            PrintUtils.printLeftJustified(out, context.smartFormatDate(each.getCreated()), 18);
            PrintUtils.printLeftJustified(out, context.smartFormatDate(each.getUpdated()), 18);
            StringBuffer permissions = new StringBuffer(50);
            if((each.getProtection() & FileProtection.ALLOW_READ) != 0) 
            	permissions.append(formatter.format("list.files.permission.read"));
            if((each.getProtection() & FileProtection.ALLOW_WRITE) != 0)
            {
                if(permissions.length() > 0)
                    permissions.append(", ");
                permissions.append(formatter.format("list.files.permission.write"));
            }
            if(permissions.length() == 0)
                permissions.append(formatter.format("list.files.permission.none"));
            PrintUtils.printLeftJustified(out, permissions.toString(), 12);
            out.print(' ');
            dc.output();
            out.println(each.getName());
        }
        dc.normal();
    }
}
