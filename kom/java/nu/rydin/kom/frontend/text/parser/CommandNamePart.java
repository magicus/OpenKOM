/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;

import nu.rydin.kom.frontend.text.Context;


/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public class CommandNamePart extends CommandLinePart
{
	private String m_cookedName;

	public CommandNamePart(String cookedName)
	{
		m_cookedName = cookedName;
	}

	public String getSeparator()
	{
		return " ";
	}

	public boolean isRequired()
	{
		return true;
	}

	protected Match innerMatch(String matchingPart, String remainder)
	{
		String cooked = cookString(matchingPart);

		if (cooked.length() == 0)
		{
			// We were fooled. Probably the user entered something like "(ful) runk".
			// Ignore this part and go get next.
			Match recursiveMatch = match(remainder);
			return new Match(recursiveMatch.isMatching(), matchingPart
					+ recursiveMatch.getRemainder(), recursiveMatch
					.getRemainder(), recursiveMatch.getParsedObject());
		}

		if (cooked.equals(m_cookedName))
		{
			return new Match(true, matchingPart, remainder, null);
		}
		else
		{
			return new Match(false, null, null, null);
		}
	}

	/* (non-Javadoc)
	 * @see nu.rydin.kom.frontend.text.parser.CommandLinePart#fillInMissingObject(nu.rydin.kom.frontend.text.Context)
	 */
	public Match fillInMissingObject(Context context) throws IOException, InterruptedException
	{
		return new Match(true, m_cookedName, null, null);
	}

}