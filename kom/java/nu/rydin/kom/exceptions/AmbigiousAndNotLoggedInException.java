package nu.rydin.kom.exceptions;

public class AmbigiousAndNotLoggedInException extends NotLoggedInException
{

    public AmbigiousAndNotLoggedInException()
    {
        super();
    }

    public AmbigiousAndNotLoggedInException(String matchedName)
    {
        super(matchedName);
    }

}
