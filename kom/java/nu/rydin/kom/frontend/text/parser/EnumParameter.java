/*
 * Created on 2004-aug-19
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nu.rydin.kom.exceptions.InvalidChoiceException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.LineEditingDoneException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;

/**
 * @author Magnus Ihse Bursie
 */
public abstract class EnumParameter extends CommandLineParameter
{
    private final List<String> m_alternatives;

    private final String m_headingKey;

    private final String m_promptKey;
    
    private final String m_legendKeyPrefix;

    private boolean m_allowPrefixes;
    
    public EnumParameter(String missingObjectQuestionKey, String headingKey,
            String promptKey, String[] alternatives, boolean allowPrefixes,
            String legendKeyPrefix, boolean isRequired)
    {
        this(missingObjectQuestionKey, headingKey, promptKey, alternatives,
                allowPrefixes, legendKeyPrefix, isRequired, null);
    }


    public EnumParameter(String missingObjectQuestionKey, String headingKey,
            String promptKey, String[] alternatives, boolean allowPrefixes,
            String legendKeyPrefix, boolean isRequired, DefaultStrategy def)
    {
        super(missingObjectQuestionKey, isRequired, def);
        m_headingKey = headingKey;
        m_promptKey = promptKey;
        m_legendKeyPrefix = legendKeyPrefix;
        m_allowPrefixes = allowPrefixes;
        m_alternatives = new ArrayList<String>(alternatives.length);
        for (int i = 0; i < alternatives.length; i++)
        {
            String each = alternatives[i];
            if(each != null)
                m_alternatives.add(each);
        }
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
        String cooked = cookString(matchingPart);

        if (cooked.length() > 0)
        {
            // well, this _could_ be a flag... check it later
            return new Match(true, matchingPart, remainder, cooked);
        } else
        {
            return new Match(false, null, null, null);
        }
    }

    public Object resolveFoundObject(Context context, Match match)
            throws IOException, InterruptedException, KOMException
    {
        int selected = Parser.resolveString(context, match.getMatchedString(),
                m_alternatives, m_headingKey, m_promptKey, m_allowPrefixes, m_legendKeyPrefix);
        return new Integer(selected);
    }

    public Match fillInMissingObject(Context context)
            throws InvalidChoiceException, OperationInterruptedException,
            IOException, InterruptedException
    {
        PrintWriter out = context.getOut();
        LineEditor in = context.getIn();
        MessageFormatter fmt = context.getMessageFormatter();

        for(;;)
        {
            try
            {
		        out.print(fmt.format(m_missingObjectQuestionKey)
		                + fmt.format("parser.parameter.enum.prompt.listall"));
		        out.flush();
		        String line = in.readLine();
		        if (line.length() == 0)
		        {
		            throw new OperationInterruptedException();
		        }
		        if (line.trim().equals("?"))
		        {
		            int selection = Parser.askForResolution(context, m_alternatives,
		                    m_promptKey, false, m_headingKey, m_allowPrefixes, m_legendKeyPrefix);
		            return innerMatch((String) m_alternatives.get(selection), "");
		        } else
		        {
		            Match newMatch = innerMatch(line, "");
		            return newMatch;
		        }
            }
            catch(LineEditingDoneException e)
            {
                continue;
            }
        }
    }

}