package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.structs.MessageSearchResult;

public class SelectAndListComments extends ListComments
{

    public SelectAndListComments(Context context, String fullName,
            long permissions)
    {
        super(context, fullName, permissions);
    }
    
    @Override
    protected void processMessageResult(Context context, MessageSearchResult[] msr)
    {
        context.getSession().getSelectedMessages().setMessages(msr);
    }

}
