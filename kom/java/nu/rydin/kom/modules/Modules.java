/*
 * Created on Oct 11, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.modules;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nu.rydin.kom.exceptions.NoSuchModuleException;

/**
 * @author Pontus Rydin
 */
public class Modules
{
    private static final Map s_modules = Collections.synchronizedMap(new HashMap());
    
    public static void registerModule(String name, Module module)
    {
        s_modules.put(name, module);
    }
    
    public static void unregisterModule(String name)
    {
        s_modules.remove(name);
    }
    
    public static Module getModule(String name)
    throws NoSuchModuleException
    {
        Module module = (Module) s_modules.get(name);
        if(module == null)
            throw new NoSuchModuleException(name);
        return module;
    }
    
    public static Set listModuleNames()
    {
        return s_modules.keySet();
    }
}
