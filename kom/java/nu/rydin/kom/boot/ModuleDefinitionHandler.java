/*
 * Created on Sep 18, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.boot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ModuleDefinitionHandler extends DefaultHandler
{
    private static final int STATE_INITIAL		= 0;
    private static final int STATE_MODULE_LIST	= 1;
    private static final int STATE_MODULE		= 2;
    private static final int STATE_CLASSPATH    = 3;
    
    private int m_state = STATE_INITIAL;
    
    private String m_moduleName;
    private String m_className;
    private Map<String, String> m_parameters;
    private List<ModuleDefinition> m_modules = new ArrayList<ModuleDefinition>();
    private List<String> m_classPath;
    
    public ModuleDefinition[] getModuleDefinitions()
    {
        ModuleDefinition[] answer = new ModuleDefinition[m_modules.size()];
        m_modules.toArray(answer);
        return answer;
    }

    public void startElement (String namespaceURI, String localName, String qName, Attributes attr)
    throws SAXException
    {
        switch(m_state)
        {
    	case STATE_INITIAL:
    	    // We're expecting a "modules" element. Everything else 
    	    // is an error
    	    //
    	    if("modules".equals(qName))
    	        m_state = STATE_MODULE_LIST;
    	    else
    	        throw new SAXException("Uexpected tag: " + qName);
    	    break;
        case STATE_MODULE_LIST:
    	    // We're expecting a "module" element. Everything else 
    	    // is an error
    	    //
    	    if(!"module".equals(qName))
    	        throw new SAXException("Uexpected tag: " + qName);
    	    
    	    // Extract attributes
    	    //
    	    m_moduleName = attr.getValue("name");
    	    m_className = attr.getValue("class");
    	    m_parameters = new HashMap<String, String>();
    	    m_state = STATE_MODULE;
    	    break;
        case STATE_MODULE:
            if("parameter".equals(qName))
                m_parameters.put(attr.getValue("name"), attr.getValue("value"));
            else if("classpath".equals(qName))
            {
                m_state = STATE_CLASSPATH;
                m_classPath = new LinkedList<String>();
            }
            else
                throw new SAXException("Uexpected tag: " + qName);
            break;
        case STATE_CLASSPATH:
            if(!"pathelement".equals(qName))
                throw new SAXException("Uexpected tag: " + qName);
            m_classPath.add(attr.getValue("location"));
            break;            
        }
    }
    
    public void endElement (String namespaceURI, String localName, String qName)
    throws SAXException
    {
        switch(m_state)
        {
        case STATE_MODULE_LIST:
            m_state = STATE_INITIAL;
            break;
        case STATE_MODULE:
            if("module".equals(qName))
            {
	            m_modules.add(new ModuleDefinition(m_moduleName, m_className, m_classPath, m_parameters));
	            m_state = STATE_MODULE_LIST;
            }
            break;
        case STATE_CLASSPATH:
            if("classpath".equals(qName))
                m_state = STATE_MODULE;
        }
    }
}
