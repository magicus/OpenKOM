/*
 * Created on Nov 17, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.constants.Visibilities;
import nu.rydin.kom.exceptions.*;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin </a>
 */
public class NamePicker
{
    private static NameAssociation innerResolveName(String name, short kind,
            Context ctx) throws KOMException, UnexpectedException,
            IOException, InterruptedException, OperationInterruptedException,
            InvalidChoiceException
    {
        try
        {
            long id = Long.parseLong(name);
            if (id > 0)
            {
                return new NameAssociation(id, (String) null);
            }
        } catch (NumberFormatException e)
        {
            // Not a numeric ID, need to parse and resolve it.
        }
        NameAssociation[] assocs = kind == -1 ? ctx.getSession()
                .getAssociationsForPattern(name) : ctx.getSession()
                .getAssociationsForPatternAndKind(name, kind);

        // Check if the mailbox name matches and include it in that case
        //
        String mailboxName = ctx.getMessageFormatter().format(
                "misc.mailboxtitle");
        if (NameUtils.match(name, mailboxName, false))
        {
            // It's the mailbox! Create an association to the logged-in person
            //
            int top = assocs.length;
            NameAssociation[] newBuff = new NameAssociation[top + 1];
            System.arraycopy(assocs, 0, newBuff, 0, top);
            long me = ctx.getLoggedInUserId();
            newBuff[top] = new NameAssociation(me, new Name(mailboxName, Visibilities.PUBLIC));
            assocs = newBuff;
        }
        if (assocs.length == 0)
        {
            throw new ObjectNotFoundException(name);
        }
        if (assocs.length == 1)
        {
            return assocs[0];
        }

        // Ambiguous! Go get possible names!
        //
        NameAssociation assoc = pickName(assocs, ctx);
        if (assoc.getId() == -1)
        {
            throw new ObjectNotFoundException(name);
        }
        return assoc;
    }

    public static long resolveNameToId(String name, short kind, Context context)
            throws ObjectNotFoundException, UnexpectedException, IOException,
            InterruptedException, OperationInterruptedException,
            KOMException
    {
        return innerResolveName(name, kind, context).getId();
    }

    public static NameAssociation resolveName(String name, short kind,
            Context context) throws ObjectNotFoundException,
            UnexpectedException, IOException, InterruptedException,
            OperationInterruptedException, KOMException
    {
        NameAssociation assoc = innerResolveName(name, kind, context);
        if (assoc.getName() == null)
        {
            long id = assoc.getId();
            return new NameAssociation(id, context.getSession().getName(id));
        }
        return assoc;
    }

    public static NameAssociation pickName(NameAssociation[] assocs, Context ctx) throws IOException, InterruptedException, KOMException {
        List candidates = new ArrayList(assocs.length);
        for (int i = 0; i < assocs.length; i++)
        {
            candidates.add(assocs[i].getName());
        }
        
        assocs[0].getName();
        int selection = Parser.askForResolution(ctx, candidates, "name.chose", true, "name.ambiguous", false);
        return assocs[selection];
    }

}