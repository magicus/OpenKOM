/*
 * Created on Jul 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom;

import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class KOMRuntimeException extends RuntimeException 
{
	public KOMRuntimeException()
	{
		super();
	}
	
	public KOMRuntimeException(String msg)
	{
		super(msg);
	}
	
	public KOMRuntimeException(Throwable t)
	{
		super(t);
	}	
	
	public KOMRuntimeException(String msg, Throwable t)
	{
		super(msg, t);
	}
	
	public String formatMessage(Context context)
	{
		return context.getMessageFormatter().format(this.getFormatKey());
	}
	
	public String formatMessage(Context context, Object arg)
	{
		return context.getMessageFormatter().format(this.getFormatKey(), arg);
	}
	
	public String formatMessage(Context context, Object[] args)
	{
		return context.getMessageFormatter().format(this.getFormatKey(), args);
	}
	
	protected String getFormatKey()
	{
		return this.getClass().getName() + ".format";
	}

}
