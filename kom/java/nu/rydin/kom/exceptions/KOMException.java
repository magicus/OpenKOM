/*
 * Created on Oct 5, 2003
 * 
 * Distributed under the GPL license. See www.gnu.org for details
 */
package nu.rydin.kom.exceptions;

import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class KOMException extends Exception
{
    protected Object[] m_msgArgs;
    
	public KOMException()
	{
		super();
	}
	
	public KOMException(String msg)
	{
		super(msg);
	}
	
	public KOMException(Object[] msgArgs) 
	{
	    m_msgArgs = msgArgs;
	}
	
	public KOMException(Throwable t)
	{
		super(t);
	}	
	
	public KOMException(String msg, Throwable t)
	{
		super(msg, t);
	}
	
	public String formatMessage(Context context)
	{
        if (m_msgArgs != null) 
	        return context.getMessageFormatter().format(this.getArgsFormatKey(), m_msgArgs);
	    return context.getMessageFormatter().format(this.getFormatKey());
	}
	
	protected String getFormatKey()
	{
		return this.getClass().getName() + ".format";
	}
	
	protected String getArgsFormatKey()
	{
		return this.getClass().getName() + ".args.format";
	}
}
