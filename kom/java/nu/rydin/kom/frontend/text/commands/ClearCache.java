/*
 * Created on Jan 16, 2005
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author Pontus Rydin
 */
public class ClearCache extends AbstractCommand
{
    public ClearCache(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        context.getSession().clearCache();
        context.getOut().println(context.getMessageFormatter().format("clear.cache.confirmation"));
    }
}
