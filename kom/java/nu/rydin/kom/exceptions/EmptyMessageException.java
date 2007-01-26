package nu.rydin.kom.exceptions;

public class EmptyMessageException extends UserException
{

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
