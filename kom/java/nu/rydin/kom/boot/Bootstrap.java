/*
 * Created on Sep 17, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.boot;

import java.io.IOException;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import nu.rydin.kom.backend.ServerSessionFactoryImpl;
import nu.rydin.kom.exceptions.NoSuchModuleException;
import nu.rydin.kom.modules.Module;
import nu.rydin.kom.modules.Modules;
import nu.rydin.kom.utils.Logger;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Bootstrap
{
    private static final long s_bootTime = System.currentTimeMillis();
    
	static
	{
	    //	  We always run in UTC.
	    //
	    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	// private Map m_modules;
	
	public static long getBootTime()
	{
	    return s_bootTime;
	}
	
	public void start()
	throws IOException, ParserConfigurationException, SAXException
	{
	    // Load class names
	    //
	    ModuleDefinition[] definitions = this.loadModuleList();
	    int top = definitions.length;
	    for(int idx = 0; idx < top; ++idx)
	    {
	        ModuleDefinition each = definitions[idx];
	        Logger.info(this, "Starting server "  + each.getName());
	        try
	        {
		        Module module = each.newInstance();
		        module.start(each.getParameters());
		        Modules.registerModule(each.getName(), module);
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
	     for(Iterator itor = Modules.listModuleNames().iterator(); itor.hasNext();)
	     {
	         try
	         {
	             Modules.getModule(itor.next().toString()).join();
	         }
	         catch(NoSuchModuleException e)
	         {
	             // Just skip
	         }
	     }
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
