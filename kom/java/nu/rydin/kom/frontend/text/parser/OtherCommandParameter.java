/*
 * Created on 2004-aug-19
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;

/**
 * OtherCommandParameter represents a parameter that accepts a (shortened) name
 * of another command.
 * 
 * The "return type" for an OtherCommandParameter is a Command object.
 * 
 * @author Magnus Ihse Bursie
 */
public class OtherCommandParameter extends CommandLineParameter
{
    public OtherCommandParameter(String missingObjectQuestionKey, boolean isRequired)
    {
        this(missingObjectQuestionKey, isRequired, null);
    }

    public OtherCommandParameter(boolean isRequired)
    {
        this(isRequired, null);
    }

    public OtherCommandParameter(String missingObjectQuestionKey, boolean isRequired, DefaultStrategy def)
    {
        super(missingObjectQuestionKey, isRequired, def);
    }

    public OtherCommandParameter(boolean isRequired, DefaultStrategy def)
    {
        super("parser.parameter.command.ask", isRequired, def);
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.command.description";
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
        String cooked = cookString(matchingPart);

        if (cooked.length() > 0)
        {
            // well, this _could_ be a command... check it later
            return new Match(true, matchingPart, remainder, cooked);
        } 
        return new Match(false, null, null, null);
    }

    public Object resolveFoundObject(Context context, Match match)
            throws IOException, InterruptedException, KOMException
    {
        Parser parser = context.getParser();
        Command command = parser.getMatchingCommand(context, match.getMatchedString());
        return command;
    }
}