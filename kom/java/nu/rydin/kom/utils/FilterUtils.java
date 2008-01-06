/*
 * Created on Oct 19, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;

import nu.rydin.kom.exceptions.UnexpectedException;

/**
 * @author Pontus Rydin
 */
public class FilterUtils
{
    public static interface Filter
    {
        public boolean include(Object obj)
        throws UnexpectedException;
    }
    
    public static Object[] applyFilter(Object[] data, Filter filter)
    throws UnexpectedException
    {
        // We're lazy: Assume nothing gets wiped out and
        // reallocate array only if we need to.
        //
        ArrayList list = null;
        int top = data.length;
        for (int idx = 0; idx < top; idx++)
        {
            Object obj = data[idx];
            if(!filter.include(obj))
            {
                // Is this the first invisible name we see? 
                // Allocate a list!
                //
                if(list == null)
                {
                    list = new ArrayList(top - 1);
                    for(int idx2 = 0; idx2 < idx; ++idx2)
                        list.add(data[idx2]);
                }
            }
            else
            {
                if(list != null)
                    list.add(obj);
            }
        }
        
        // So? Was the list untouched? Good in that case!
        //
        if(list == null)
            return data;
        
        // Create array of the correct derived type.
        //
        data = (Object[]) Array.newInstance(data.getClass().getComponentType(), list.size());
        list.toArray(data);
        return data;
    }
}
