/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.structs.TextNumber;

/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class TextNumberParameter extends CommandLineParameter
{
	public TextNumberParameter(String missingObjectQuestionKey, boolean isRequired)
	{
		super(missingObjectQuestionKey, isRequired);
	}

	public TextNumberParameter(boolean isRequired)
	{
		super("parser.parameter.textnumber.ask", isRequired);
	}

	protected Match innerMatch(String matchingPart, String remainder)
	{
		boolean global = false;
		String cooked = matchingPart.trim();
		
		int top = cooked.length();
		
		if (top == 0)
		{
		    // I don't think this can actually happen, but better safe than sorry...
		    return new Match(false, null, null, null);
		}
		
		if ((cooked.length() > 2) && (cooked.charAt(0) == '(') && (cooked.charAt(top-1) == ')'))
		{
		    // It might be a global number!
		    global = true;
		    cooked = cooked.substring(1, top-1);
		}
		
		try
		{
			int number = Integer.parseInt(cooked);
			return new Match(true, matchingPart, remainder, new TextNumber(number, global));
		}
		catch (NumberFormatException e)
		{
			return new Match(false, null, null, null);
		}
	}

    protected String getUserDescriptionKey() {
        return "parser.parameter.textnumber.description";
    }
}
