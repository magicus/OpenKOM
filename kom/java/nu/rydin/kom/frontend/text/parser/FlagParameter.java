/*
 * Created on 2004-aug-19
 *
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author Magnus Ihse 
 */
public class FlagParameter extends CommandLineParameter
{
    private final String[] m_flagLabels;

    public FlagParameter(String missingObjectQuestionKey, String[] flagLabels, boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
        m_flagLabels = flagLabels;
    }

    public FlagParameter(boolean isRequired, String[] flagLabels)
    {
        super("parser.parameter.flag.ask", isRequired);
        m_flagLabels = flagLabels;
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.flag.description";
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
        String cooked = cookString(matchingPart);

        if (cooked.length() > 0)
        {
            // well, this _could_ be a flag... check it later
            return new Match(true, matchingPart, remainder, cooked);
        } 
        else
        {
            return new Match(false, null, null, null);
        }
    }

    public Object resolveFoundObject(Context context, Match match)
            throws KOMException
    {
        String flagName = match.getMatchedString();
        int imatch = -1;
        int top = m_flagLabels.length;
        for (int idx = 0; idx < top; ++idx)
        {
            if (m_flagLabels[idx] != null
                    && NameUtils.match(flagName, m_flagLabels[idx]))
            {
                // Ambigous?
                //
                if (imatch != -1)
                {
                    // FIXME:Ihse: We should resolve the ambiguity instead!
                    throw new AmbiguousNameException();
                }
                imatch = idx;
            }
        }
        if (imatch == -1)
        {
            throw new ObjectNotFoundException(context.getMessageFormatter()
                    .format("manipulate.flag.nonexistent", flagName));
        }

        return new Integer(imatch);
    }
}