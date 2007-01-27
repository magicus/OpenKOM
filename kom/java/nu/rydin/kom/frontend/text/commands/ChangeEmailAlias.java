package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

public class ChangeEmailAlias extends AbstractCommand
{
    public ChangeEmailAlias(Context context, String fullName, long permissions)
    {
        super(fullName, new CommandLineParameter[] { 
                new NamedObjectParameter(true),
                new RawParameter("change.email.alias.ask.1", true) }, permissions);       
    }

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        MessageFormatter fmt = context.getMessageFormatter();
        KOMWriter out = context.getOut();
        NameAssociation object = (NameAssociation) parameters[0];
        context.getSession().changeEmailAlias(object.getId(), (String) parameters[1]);
        out.println(fmt.format("change.email.alias.confirm", context.formatObjectName(object)));
    }
}
