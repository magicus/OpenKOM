package nu.rydin.kom.exceptions;

public class AmbigiousAndNotLoggedInException extends NotLoggedInException
{
    static final long serialVersionUID = 2005;
    
    public AmbigiousAndNotLoggedInException()
    {
        super();
    }

    public AmbigiousAndNotLoggedInException(String matchedName)
    {
        super(matchedName);
    }

}
