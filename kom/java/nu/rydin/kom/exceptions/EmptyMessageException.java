package nu.rydin.kom.exceptions;

public class EmptyMessageException extends UserException
{
    static final long serialVersionUID = 2005;
    
	public EmptyMessageException()
	{
	}

	public EmptyMessageException(String message)
	{
		super(message);
	}

	public EmptyMessageException(Object[] msgArgs)
	{
		super(msgArgs);
	}
}
