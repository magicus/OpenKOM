/*
 * Created on Sep 13, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class ConferenceWildcardParameter extends NamedObjectParameter
{
    public final static NameAssociation ALL_CONFS = new NameAssociation(-1,
            "*", NameManager.CONFERENCE_KIND);

    public ConferenceWildcardParameter(String missingObjectQuestionKey,
            boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
    }

    public ConferenceWildcardParameter(boolean isRequired)
    {
        super(isRequired);
    }

    public Object resolveFoundObject(Context context, Match match)
            throws KOMException, IOException, InterruptedException
    {
        String pattern = match.getMatchedString();
        if (pattern.equals("*"))
        {
            return ALL_CONFS;
        }
        else
        {
            return super.resolveFoundObject(context, match);
        }
    }

    protected boolean isValidName(String name)
    {
        return (super.isValidName(name) || name.equals("*"));
    }
}
