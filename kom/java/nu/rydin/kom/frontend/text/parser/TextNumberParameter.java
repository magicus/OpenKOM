/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.structs.TextNumber;


/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class TextNumberParameter extends CommandLineParameter
{
	private boolean m_isRequired;
	
	public TextNumberParameter(String missingObjectQuestionKey, boolean isRequired)
	{
		super(missingObjectQuestionKey);
		m_isRequired = isRequired;
	}

	public TextNumberParameter(boolean isRequired)
	{
		super("parser.parameter.textnumber.ask");
		m_isRequired = isRequired;
	}

	public boolean isRequired()
	{
		return m_isRequired;
	}

	protected Match innerMatch(String matchingPart, String remainder)
	{
		boolean global = false;
		String cooked = cookString(matchingPart);
		if (cooked.charAt(0) == '#') {
			// It's a global text number
			global = true;
			cooked = cooked.substring(1);
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

}
