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
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;

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
    
	public Match fillInMissingObject(Context context) 
	throws InvalidChoiceException, OperationInterruptedException, IOException, InterruptedException
	{
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();

		out.print(fmt.format(m_missingObjectQuestionKey) + fmt.format("parser.parameter.enum.prompt.listall"));
		out.flush();
		String line = in.readLine();
		if(line.length() == 0) {
		    throw new OperationInterruptedException();
		}
		if(line.trim().equals("?")) {
			int selection = Parser.askForResolution(context, m_alternatives, m_promptKey, false, m_headingKey);
			return innerMatch((String) m_alternatives.get(selection), "");
		} else {
			Match newMatch = innerMatch(line, "");
			return newMatch;
		}
	}

    
}