/*
 * Created on Aug 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.io.IOException;

import nu.rydin.kom.backend.data.NameManager;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.NamePicker;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author Henrik Schröder
 *
 */
public class NamedObjectEllipsisParameter extends EllipsisParameter
{

    public NamedObjectEllipsisParameter(String missingObjectQuestionKey,
            boolean isRequired, NamedObjectParameter innerParameter)
    {
        super(missingObjectQuestionKey, isRequired, innerParameter);
    }

    public Object resolveFoundObject(Context context, Match match)
    throws KOMException, IOException, InterruptedException
    {
        Object[] parsedObjects = (Object[])match.getParsedObject();
        NameAssociation[] result = new NameAssociation[parsedObjects.length];
        
        for (int i = 0; i < parsedObjects.length; i++)
        {
            result[i] = NamePicker.resolveName((String)parsedObjects[i], NameManager.UNKNOWN_KIND, context);
        }
        return result;
    }
    
    public char getSeparator()
    {
        return ':';
    }
}
