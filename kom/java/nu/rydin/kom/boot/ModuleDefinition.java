/*
 * Created on Sep 18, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.boot;

import java.util.Map;

import nu.rydin.kom.modules.Module;

/**
 * Represents a module definition.
 * 
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
class ModuleDefinition
{
    /**
     * The name of the module
     */
    private String name;
    
    /**
     * Class of module implementation
     */
    private String className;
    
    /**
     * Module specific parameters
     */
    private Map parameters;
    
    /**
     * Consructs a new module definition.
     * 
     * @param name The name of the module
     * @param className The module implementation class
     * @param parameters Module specific parameters
     */
    public ModuleDefinition(String name, String className, Map parameters)
    {
        this.name 		= name;
        this.className 	= className;
        this.parameters	= parameters;
    }
    
    /**
     * Returns the module implementation class name.
     */
    public String getClassName()
    {
        return className;
    }
    
    /**
     * Returns the module instance name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns module specific parameters
     */
    public Map getParameters()
    {
        return parameters;
    }
    
    public Module newInstance()
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return (Module) Class.forName(this.className).newInstance();
    }
}
