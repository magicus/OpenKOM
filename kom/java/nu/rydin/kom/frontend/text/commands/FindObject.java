package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

public class FindObject extends AbstractCommand
{
    public FindObject(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { new RawParameter("find.object.ask.0", true) }, permissions);       
    }

    public void execute(Context context, Object[] parameters)
    throws KOMException, IOException, InterruptedException
    {
        // Set up
        //
        KOMWriter out = context.getOut();
        MessageFormatter fmt = context.getMessageFormatter();
        DisplayController dc = context.getDisplayController();
        
        // Find objects
        //
        NameAssociation[] hits = context.getSession().findObjects((String) parameters[0]);
        
        // List objects
        //
        dc.normal();
        int top = hits.length;
        if(top > 0)
        {
            out.println(fmt.format("find.object.matches", new Integer(top)));
            out.println();
            dc.output();
            for(int idx = 0; idx < top; ++idx)
                out.println(context.formatObjectName(hits[idx]));
        }
        else
            out.println(fmt.format("find.object.no.match"));
        dc.normal();
        
    }
}
