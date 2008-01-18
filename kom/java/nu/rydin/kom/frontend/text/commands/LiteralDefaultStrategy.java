package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.DefaultStrategy;

public class LiteralDefaultStrategy implements DefaultStrategy
{
    private String value;

    public LiteralDefaultStrategy(String value)
    {
        super();
        this.value = value;
    }

    public String getDefault(Context context) throws UnexpectedException
    {
        return value;
    }

}
