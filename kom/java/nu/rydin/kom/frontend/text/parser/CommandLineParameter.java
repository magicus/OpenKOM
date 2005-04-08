/*
 * Created on Aug 8, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.InvalidChoiceException;
import nu.rydin.kom.exceptions.LineEditingDoneException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;


/**
 * @author Magnus Ihse (magnus@ihse.net)
 */
public abstract class CommandLineParameter extends CommandLinePart
{

	protected String m_missingObjectQuestionKey;
    protected boolean m_isRequired;
	
	public CommandLineParameter(String missingObjectQuestionKey, boolean isRequired)
	{
		m_missingObjectQuestionKey = missingObjectQuestionKey;
		m_isRequired = isRequired;
	}
	
	public char getSeparator()
	{
		return ',';
	}

	public Match fillInMissingObject(Context context) 
	throws IOException, InterruptedException, OperationInterruptedException, InvalidChoiceException
	{
		PrintWriter out = context.getOut();
		LineEditor in = context.getIn();
		MessageFormatter fmt = context.getMessageFormatter();
	    for(;;)
	    {
	        try
	        {
				out.println();
				out.print(fmt.format(m_missingObjectQuestionKey));
				out.flush();
				String line = in.readLine();
				if(line.length() == 0)
				    throw new OperationInterruptedException();
				Match newMatch = innerMatch(line, "");
				return newMatch;
	        }
	        catch(LineEditingDoneException e)
	        {
	            continue;
	        }
	    }
	}

    public boolean isRequired() 
    {
    	return m_isRequired;
    }

    public String getUserDescription(Context context) 
    {
		MessageFormatter fmt = context.getMessageFormatter();
        if (isRequired()) 
            return "<" + fmt.format(getUserDescriptionKey()) + ">";
        else
            return "[" + fmt.format(getUserDescriptionKey()) + "]";
    }

    protected abstract String getUserDescriptionKey();
}
