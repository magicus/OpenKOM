/*
 * Created on 2004-aug-20
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.editor.EditorContext;

/**
 * @author Magnus Ihse Bursie
 */
public class CommandNotFoundException extends UserException {

    public CommandNotFoundException() {
        super();
    }

    public CommandNotFoundException(String msg) {
        super(msg);
    }
    
	public CommandNotFoundException(Object[] msgArgs) 
	{
	    super(msgArgs);
	}

	public String formatMessage(Context context)
	{
	    if (context instanceof EditorContext)
	    {
	        if (m_msgArgs != null) 
		        return context.getMessageFormatter().format(this.getArgsFormatKey() + ".editor", m_msgArgs);
		    return context.getMessageFormatter().format(this.getFormatKey() + ".editor");
	    }
        if (m_msgArgs != null) 
	        return context.getMessageFormatter().format(this.getArgsFormatKey(), m_msgArgs);
	    return context.getMessageFormatter().format(this.getFormatKey());	        
	}
}
