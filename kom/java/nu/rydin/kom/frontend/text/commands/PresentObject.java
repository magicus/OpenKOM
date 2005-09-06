/*
 * Created on Jun 12, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.AuthorizationException;
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
	public PresentObject (Context context, String fullname, long permissions)
	{
		super(fullname, new CommandLineParameter[] { new NamedObjectParameter(true)}, permissions);
	}
	
	public void execute(Context context, Object[] parameterArray)
	throws KOMException, IOException, InterruptedException 
	{
	    NameAssociation nameAssociation = (NameAssociation) parameterArray[0];
	    
		long objectId = nameAssociation.getId();
		ServerSession ss = context.getSession();
		if(!ss.canManipulateObject(objectId))
		    throw new AuthorizationException();
		UnstoredMessage msg = context.getMessageEditor().
			edit(-1, -1, -1, null, -1, null, nameAssociation.getName().getName());
		MessageOccurrence occ = ss.storePresentation(msg, objectId);
        context.getOut().println(
                context.getMessageFormatter().format("write.message.saved",
                        new Object[] { new Integer(occ.getLocalnum()), new Long(occ.getGlobalId())} ));
	}
}
