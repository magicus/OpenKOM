/*
 * Created on Sep 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class MessageFomatter
{
    private static final String BUNDLE_NAME = "nu.rydin.kom.i18n.messages";//$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private MessageFomatter()
    {
    }

    public static String getString(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }
}