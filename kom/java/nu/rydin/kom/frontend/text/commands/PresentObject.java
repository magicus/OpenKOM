/*
 * Created on Jun 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.BadParameterException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.backend.NameUtils;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class PresentObject extends AbstractCommand 
{
	public PresentObject (String fullname)
	{
		super(fullname);
	}
	
	public void execute(Context context, String[] parameters)
	throws KOMException, IOException, InterruptedException 
	{
		if (0 == parameters.length)
		{
			throw new BadParameterException();
		}
		long objectId = NamePicker.resolveName(NameUtils.assembleName(parameters), (short) -1, context);
		short kind = context.getSession().getObjectKind(objectId);
		UnstoredMessage msg = context.getMessageEditor().edit(context, -1);
		MessageOccurrence occ = context.getSession().storeMagicMessage(msg, kind, objectId);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}

	public boolean acceptsParameters()
	{
		return true;
	}
}
