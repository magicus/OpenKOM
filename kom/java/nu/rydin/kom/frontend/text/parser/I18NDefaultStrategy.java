/*
 * Created on Jan 17, 2008
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author Pontus Rydin
 */
public class I18NDefaultStrategy implements DefaultStrategy
{
    private String key;

    public I18NDefaultStrategy(String key)
    {
        super();
        this.key = key;
    }

    public String getDefault(Context context) throws UnexpectedException
    {
        return context.getMessageFormatter().format(key);
    }

}
