/*
 * Created on Sep 11, 2005
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
public interface DefaultStrategy
{
    public String getDefault(Context context)
    throws UnexpectedException;
}
