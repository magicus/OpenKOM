/*
 * Created on 2004-aug-19
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author Magnus Ihse 
 */
public abstract class EnumParameter extends CommandLineParameter
{
    private final List m_alternatives;
    private final String m_headingKey;
    private final String m_promptKey;

    public EnumParameter(String missingObjectQuestionKey, String headingKey, String promptKey, String[] alternatives, boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
        m_headingKey = headingKey;
        m_promptKey = promptKey;
        m_alternatives = new ArrayList(alternatives.length);
        for (int i = 0; i < alternatives.length; i++)
        {
            m_alternatives.add(alternatives[i]);
        }
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
            throws IOException, InterruptedException, KOMException
    {
        int selected = Parser.resolveString(context, match.getMatchedString(), m_alternatives, m_headingKey, m_promptKey);
        return new Integer(selected);
    }
}