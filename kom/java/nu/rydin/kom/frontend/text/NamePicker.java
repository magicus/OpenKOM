/*
 * Created on Nov 17, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.*;
import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class NamePicker
{
	private static NameAssociation innerResolveName(String name, short kind, Context ctx)
	throws ObjectNotFoundException, UnexpectedException, IOException, 
		InterruptedException, OperationInterruptedException, InvalidChoiceException
	{
		try
		{
			long id = Long.parseLong(name);
			return new NameAssociation(id, null);
		}
		catch (NumberFormatException e)
		{
			// Not a numeric ID, need to parse and resolve it. 
		}
		NameAssociation[] assocs = kind == -1
			? ctx.getSession().getAssociationsForPattern(name)
			: ctx.getSession().getAssociationsForPatternAndKind(name, kind);
			
		// Check if the mailbox name matches and include it in that case
		//
		String mailboxName = ctx.getMessageFormatter().format("misc.mailboxtitle");
		if(NameUtils.match(name, mailboxName))
		{
			// It's the mailbox! Create an association to the logged-in person
			//
			int top = assocs.length;
			NameAssociation[] newBuff = new NameAssociation[top + 1];
			System.arraycopy(assocs, 0, newBuff, 0, top);
			long me = ctx.getLoggedInUserId();
			newBuff[top] = new NameAssociation(me, mailboxName);
			assocs = newBuff; 	
		}
		if(assocs.length == 0)
			throw new ObjectNotFoundException(name);
		if(assocs.length == 1)
			return assocs[0];
		
		// Ambiguous! Go get possible names!
		//
		NameAssociation assoc = pickName(assocs, ctx);
		if(assoc.getId() == -1)
			throw new ObjectNotFoundException(name);
		return assoc;
	}
	
	public static long resolveNameToId(String name, short kind, Context context) throws ObjectNotFoundException, UnexpectedException, IOException, 
	InterruptedException, OperationInterruptedException, InvalidChoiceException {
		return innerResolveName(name, kind, context).getId();
	}
	
	public static NameAssociation resolveName(String name, short kind, Context context) throws ObjectNotFoundException, UnexpectedException, IOException, 
	InterruptedException, OperationInterruptedException, InvalidChoiceException {
		NameAssociation assoc = innerResolveName(name, kind, context);
		if (assoc.getName() == null) {
			long id = assoc.getId();
			return new NameAssociation(id, context.getSession().getName(id));
		}
		return assoc;
	}
	
	public static NameAssociation pickName(NameAssociation[] assocs, Context ctx)
	throws IOException, OperationInterruptedException, InterruptedException, InvalidChoiceException
	{
		MessageFormatter formatter = ctx.getMessageFormatter();
		PrintWriter out = ctx.getOut();
		LineEditor in = ctx.getIn();
		out.println(formatter.format("name.ambiguous"));
		int top = assocs.length;
		for(int idx = 0; idx < top; ++idx)
		{
			out.print(idx + 1);
			out.print(". ");
			out.println(assocs[idx].getName()); 
		}
		out.println();
		out.print(formatter.format("name.chose"));
		out.flush();
		String input = in.readLine();
		out.println();
		if(input.length() == 0)
			throw new OperationInterruptedException();
		int idx = -1;
		try
		{
			idx = Integer.parseInt(input);
		}
		catch(NumberFormatException e)
		{
			throw new InvalidChoiceException();
		}
		if(idx < 1 || idx > top)
		{
			throw new InvalidChoiceException();
		}
		return assocs[idx - 1];
	}

}
