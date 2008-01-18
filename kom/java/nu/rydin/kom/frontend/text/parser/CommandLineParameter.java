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
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.utils.Logger;


/**
 * @author Magnus Ihse Bursie (magnus@ihse.net)
 */
public abstract class CommandLineParameter extends CommandLinePart
{

	protected final String m_missingObjectQuestionKey;
    protected final boolean m_isRequired;
    protected final DefaultStrategy m_default;
	
	public CommandLineParameter(String missingObjectQuestionKey, boolean isRequired, DefaultStrategy defaultS)
	{
		m_missingObjectQuestionKey = missingObjectQuestionKey;
		m_isRequired = isRequired;
		m_default = defaultS;
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
				String defaultString = "";
				if(m_default != null)
				{
				    try
				    {
				        defaultString = m_default.getDefault(context);
				    }
				    catch(UnexpectedException e)
				    {
				        // Not being able to figure out the default is
				        // not the end of the world, so we let this one slide.
				        // However, we definitely want to log it!
				        //
				        Logger.warn(this, e);
				    }
				}
				String line = in.readLine(defaultString);
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
