/*
 * Created on Sep 18, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.boot;

import java.util.ArrayList;
import java.util.HashMap;
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
    
    private int m_state = STATE_INITIAL;
    
    private String m_moduleName;
    private String m_className;
    private Map m_parameters;
    private List m_modules = new ArrayList();
    
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
    	    m_parameters = new HashMap();
    	    m_state = STATE_MODULE;
    	    break;
        case STATE_MODULE:
    	    // We're expecting a "parameter" element. Everything else 
    	    // is an error
    	    //
    	    if(!"parameter".equals(qName))
    	        throw new SAXException("Uexpected tag: " + qName);
            m_parameters.put(attr.getValue("name"), attr.getValue("value"));
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
	            m_modules.add(new ModuleDefinition(m_moduleName, m_className, m_parameters));
	            m_state = STATE_MODULE_LIST;
            }
            break;
        }
    }
}
