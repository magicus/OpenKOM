/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;


/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public abstract class CommandLineParameter extends CommandLinePart
{

	private String m_missingObjectQuestionKey;
	
	public CommandLineParameter(String missingObjectQuestionKey)
	{
		m_missingObjectQuestionKey = missingObjectQuestionKey;
	}
	
	public String getSeparator()
	{
		return ",";
	}

	public Match getMissingObject(Context context, Match oldMatch) throws IOException, InterruptedException
	{
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();

		out.print(fmt.format(m_missingObjectQuestionKey));
		out.flush();
		String line = in.readLine();
		Match newMatch = innerMatch(line, "");
		return newMatch;
	}
}
