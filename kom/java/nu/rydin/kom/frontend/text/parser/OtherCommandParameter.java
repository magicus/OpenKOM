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
 * @author Magnus Ihse 
 */
public class OtherCommandParameter extends CommandLineParameter
{
    public OtherCommandParameter(String missingObjectQuestionKey, boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
    }

    public OtherCommandParameter(boolean isRequired)
    {
        super("parser.parameter.command.ask", isRequired);
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
        else
        {
            return new Match(false, null, null, null);
        }
    }

    public Object resolveFoundObject(Context context, Match match)
            throws IOException, InterruptedException, KOMException
    {
        Parser parser = context.getParser();
        Command command = parser.getMatchingCommand(context, match.getMatchedString());
        return command;
    }
}