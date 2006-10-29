/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.structs.MessageLocator;

/**
 * TextNumberParameter represents a parameter that accepts either a local or
 * a global text number.
 * 
 * The "return type" for a TextNumberParameter is a TextNumber object.
 * 
 * @author Magnus Ihse Bursie (magnus@ihse.net)
 */
public class TextNumberParameter extends CommandLineParameter
{
	public TextNumberParameter(String missingObjectQuestionKey, boolean isRequired)
	{
		this(missingObjectQuestionKey, isRequired, null);
	}

	public TextNumberParameter(boolean isRequired)
	{
		this(isRequired, null);
	}
	
	public TextNumberParameter(String missingObjectQuestionKey, boolean isRequired, DefaultStrategy def)
	{
		super(missingObjectQuestionKey, isRequired, def);
	}

	public TextNumberParameter(boolean isRequired, DefaultStrategy def)
	{
		super("parser.parameter.textnumber.ask", isRequired, def);
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
		    // Global number on the form "(123456)"
		    global = true;
		    cooked = cooked.substring(1, top-1);
		}

        if ((cooked.length() > 1) && (cooked.charAt(0) == '('))
        {
            // Global number on the form "(123456"
            global = true;
            cooked = cooked.substring(1, top);
        }
        
		try
		{
			int number = Integer.parseInt(cooked);
			if (number > 0)
			{
			    return new Match(true, matchingPart, remainder, 
                        global ? new MessageLocator(number) : new MessageLocator(-1, number));
			}
			else
			{
			    return new Match(false, null, null, null);
			}
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
