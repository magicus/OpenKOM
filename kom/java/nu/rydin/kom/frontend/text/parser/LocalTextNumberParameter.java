/*
 * Created on Sep 5, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

/**
 * @author Henrik Schröder
 */
public class LocalTextNumberParameter extends CommandLineParameter
{
    public LocalTextNumberParameter(String missingObjectQuestionKey,
            boolean isRequired)
    {
        super(missingObjectQuestionKey, isRequired);
    }

    public LocalTextNumberParameter(boolean isRequired)
    {
        super("parser.parameter.localtextnumber.ask", isRequired);
    }

    protected String getUserDescriptionKey()
    {
        return "parser.parameter.localtextnumber.description";
    }

    protected Match innerMatch(String matchingPart, String remainder)
    {
        String cooked = matchingPart.trim();

        try
        {
            int number = Integer.parseInt(cooked);
            if (number > 0)
            {
                return new Match(true, matchingPart, remainder, new Integer(number));
            }
        } catch (NumberFormatException e)
        {
        }
        return new Match(false, null, null, null);
    }
}