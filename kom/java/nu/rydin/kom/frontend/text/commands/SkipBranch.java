/*
 * Created on Jan 16, 2005
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author Pontus Rydin
 */
public class SkipBranch extends AbstractCommand
{
    public SkipBranch (Context context, String fullname)
	{
		super(fullname, AbstractCommand.NO_PARAMETERS);
	}
	
 	public void execute(Context context, Object[] parameterArray)
	throws KOMException
	{
 		ServerSession ss = context.getSession();
 		int n = ss.skipBranch(ss.getLastMessageHeader().getId());
 		MessageFormatter mf = context.getMessageFormatter();
 		context.getOut().println (mf.format("skip.subject.message", n));
	}
}
