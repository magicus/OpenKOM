/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;

import nu.rydin.kom.backend.NameUtils;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public abstract class CommandLinePart
{
	public static final class Match
	{
		private boolean m_isMatching;
		private String m_remainder;
		private String m_matchedString;
		private Object m_parsedObject;

		public Match(boolean isMatching, String matchedString,
				String remainder, Object parsedObject)
		{
			m_isMatching = isMatching;
			m_matchedString = matchedString;
			m_remainder = remainder;
			m_parsedObject = parsedObject;
		}
		public boolean isMatching()
		{
			return m_isMatching;
		}
		public Object getParsedObject()
		{
			return m_parsedObject;
		}
		public String getRemainder()
		{
			return m_remainder;
		}
		public String getMatchedString()
		{
			return m_matchedString;
		}
	}
	
	public abstract boolean isRequired();
	
	public abstract String getSeparator();
	
	protected abstract Match innerMatch(String matchingPart, String remainder);

	/**
	 * Note: commandLine must not be NULL.
	 * 
	 * @param commandLine 
	 * @return
	 */
	public Match match(String commandLine)
	{
		String matchingPart;
		String remainder;
	
		// Trim leading whitespace
		while (Character.isWhitespace(commandLine.charAt(0))) {
			commandLine = commandLine.substring(1);
		}
		
		if (commandLine.length() == 0) {
			return new Match(false, null, null, null);
		}
		
		int separatorPos = commandLine.indexOf(getSeparator());
		if (separatorPos == -1) {
			matchingPart = commandLine;
			remainder = "";
		} else {
			matchingPart = commandLine.substring(0, separatorPos);
			remainder = commandLine.substring(separatorPos, commandLine.length());
		}
		
		return innerMatch(matchingPart, remainder);
	}

	public Match fillInMissingObject(Context context, Match oldMatch) throws IOException, InterruptedException {
		return oldMatch;
	}
	
	public Object resolveFoundObject(Context context, Match match) throws IOException, InterruptedException {
		return match.getParsedObject();
	}

	protected String cookString(String matchingPart)
	{
		return NameUtils.normalizeName(matchingPart);
	}
}
