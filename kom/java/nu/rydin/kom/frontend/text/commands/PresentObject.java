/*
 * Created on Jun 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.UnstoredMessage;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class PresentObject extends AbstractCommand 
{
	public PresentObject (String fullname)
	{
		super(fullname, new CommandLineParameter[] { new NamedObjectParameter(true)});
	}
	
	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
	    
		long objectId = nameAssociation.getId();
		short kind = context.getSession().getObjectKind(objectId);
		UnstoredMessage msg = context.getMessageEditor().edit(context, -1);
		MessageOccurrence occ = context.getSession().storeMagicMessage(msg, kind, objectId);
		context.getOut().println(context.getMessageFormatter().format(
			"write.message.saved", new Integer(occ.getLocalnum())));
	}
}
