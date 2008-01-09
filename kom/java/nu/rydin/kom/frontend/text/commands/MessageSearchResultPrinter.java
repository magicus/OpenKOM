package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.structs.MessageSearchResult;

public interface MessageSearchResultPrinter
{

    void printSearchResultRow(Context context, KOMWriter out,
            MessageSearchResult msr);

    void printSearchResultHeader(Context context);

}
