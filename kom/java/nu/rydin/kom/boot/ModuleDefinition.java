/*
 * Created on Sep 18, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.boot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
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
    private final String name;
    
    /**
     * Class of module implementation
     */
    private final String className;
    
    /**
     * Module specific class path
     */
    private final List classPath;
    
    /**
     * Module specific parameters
     */
    private final Map parameters;
    
    /**
     * Consructs a new module definition.
     * 
     * @param name The name of the module
     * @param className The module implementation class
     * @param parameters Module specific parameters
     */
    public ModuleDefinition(String name, String className, List classPath, Map parameters)
    {
        this.name 		= name;
        this.className 	= className;
        this.classPath  = classPath;
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
     * Returns the module specific class path
     */
    public List getClassPath()
    {
        return classPath;
    }
    
    /**
     * Returns module specific parameters
     */
    public Map getParameters()
    {
        return parameters;
    }
    
    public Module newInstance()
    throws ClassNotFoundException, IllegalAccessException, InstantiationException, MalformedURLException
    {
        // Build a ClassLoader based on specified classpath (if any)
        //
        ClassLoader loader;
        if(this.classPath != null)
        {
            int top = classPath.size();
            URL[] urls = new URL[top];
            int idx = 0;
            for(Iterator itor = classPath.iterator(); itor.hasNext(); ++idx)
                urls[idx] = new File((String) itor.next()).toURL();
            loader = new URLClassLoader(urls, this.getClass().getClassLoader());
        }
        else
            loader = this.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        return (Module) loader.loadClass(this.className).newInstance();
    }
}
