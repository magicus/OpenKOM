/*
 * Created on Sep 17, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.boot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import nu.rydin.kom.backend.ServerSessionFactoryImpl;
import nu.rydin.kom.modules.Module;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Bootstrap
{
	static
	{
	    //	  We always run in UTC.
	    //
	    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

	    // Make sure the backend is running. TODO: Do this in a more elegant and remoteable way
	    //
	    ServerSessionFactoryImpl.instance();
	}
	
	private Map m_modules;
	
	public void start()
	throws IOException, ParserConfigurationException, SAXException
	{
	    // Load class names
	    //
	    ModuleDefinition[] definitions = this.loadModuleList();
	    int top = definitions.length;
	    m_modules = new HashMap(top);
	    for(int idx = 0; idx < top; ++idx)
	    {
	        ModuleDefinition each = definitions[idx];
	        Logger.info(this, "Starting server "  + each.getName());
	        try
	        {
		        Module module = each.newInstance();
		        module.start(each.getParameters());
		        m_modules.put(each.getName(), module);
	        }
	        catch(ClassNotFoundException e)
	        {
	            Logger.error(this, "Error locating server class " + each.getName(), e);
	        }
	        catch(InstantiationException e)
	        {
	            Logger.error(this, "Error creating instance of server class " + each.getName(), e);
	        }
	        catch(IllegalAccessException e)
	        {
	            Logger.error(this, "Error creating instance of server class " + each.getName(), e);
	        }	        	        
	    }
	}
	    
	 public void join()
	 throws InterruptedException
	 {
	     for(Iterator itor = m_modules.values().iterator(); itor.hasNext();)
	         ((Module) itor.next()).join();
	}
	 
	 public void run()
	 throws InterruptedException, IOException, ParserConfigurationException, SAXException
	 {
	     // Start everything and wait for the world (as we know it) to end
	     //
	     this.start();
	     this.join();
	 }
	 
	 public static void main(String[] args)
	 {
	     Bootstrap me = new Bootstrap();
	     try
	     {
	         me.run();
	     }
	     catch(Throwable t)
	     {
	         Logger.error(me, t);
	     }
	 }
	 
	 protected ModuleDefinition[] loadModuleList()
	 throws IOException, SAXException, ParserConfigurationException
	 {
	     ModuleDefinitionHandler mh = new ModuleDefinitionHandler();
		    
        XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xr.setContentHandler(mh);
        xr.parse (new InputSource (ClassLoader.getSystemClassLoader().
                getResourceAsStream("modules.xml")));
        return mh.getModuleDefinitions();		   
	 }
}
