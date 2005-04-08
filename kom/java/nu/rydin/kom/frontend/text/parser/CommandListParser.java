/*
 * Created on Nov 8, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Pontus Rydin
 */
public class CommandListParser extends DefaultHandler
{
    public static final short STATE_INITIAL			= 0;
    public static final short STATE_COMMAND_LIST	= 1;
    public static final short STATE_CATEGORY		= 2;
    public static final short STATE_COMMAND			= 3;
    
    private static final Class[] s_commandCtorSignature = new Class[]
          { Context.class, String.class, long.class };

    private final Map m_categories 	= new HashMap();
    
    private final List m_commands	= new ArrayList();
    
    private final Context m_context;
    
    private short m_state = STATE_INITIAL;
    
    private CommandCategory m_currentCat;
    
    public CommandListParser(Context context)
    {
        super();
        m_context = context;
    }
    
    
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException
    {
        switch(m_state)
        {
        case STATE_INITIAL:
            // Looking for "parser" node
            //
            if(!"commandlist".equals(qName))
                throw new SAXException("Node 'commandlist' expected");
            m_state = STATE_COMMAND_LIST;
            break;
        case STATE_COMMAND_LIST:
            if(!"category".equals(qName))
                throw new SAXException("Node 'category' expected");
            m_currentCat = new CommandCategory(
                    atts.getValue("id"), atts.getValue("i18n"),
                    Integer.parseInt(atts.getValue("order")));
            m_categories.put(m_currentCat.getId(), m_currentCat);
            m_state = STATE_CATEGORY;
            break;
        case STATE_CATEGORY:
        {
            try
            {
	            if(!"command".equals(qName))
	                throw new SAXException("Node 'command' expected");
	            String className = atts.getValue("class");
	            Class clazz = Class.forName(className);
	            Constructor ctor = clazz.getConstructor(s_commandCtorSignature);
	            String pString = atts.getValue("permissions");
	            long permissions = pString != null ? Long.parseLong(pString, 16) : 0L;
	
	            // Install primary command
	            //
	            MessageFormatter formatter = m_context.getMessageFormatter();
	            String name = formatter.format(className + ".name");
	            
	            Command primaryCommand = (Command) ctor
	                    .newInstance(new Object[]
	                    { m_context, name, new Long(permissions) });
	            m_commands.add(primaryCommand);
	            m_currentCat.addCommand(primaryCommand);
	
	            // Install aliases
	            //
	            int aliasIdx = 1;
	            for(;; ++aliasIdx)
	            {
	                // Try alias key
	                //
	                String alias = formatter.getStringOrNull(clazz.getName()
	                        + ".name." + aliasIdx);
	                if (alias == null)
	                    break; // No more aliases
	
	                // We found an alias! Create command.
	                //
	                Command aliasCommand = (Command) ctor
	                        .newInstance(new Object[]
	                        { m_context, alias, new Long(permissions) });
	                m_commands.add(aliasCommand);
	                m_currentCat.addCommand(aliasCommand);
	            }
	            m_state = STATE_COMMAND;
	            break;
            }
            catch (ClassNotFoundException e)
            {
                throw new SAXException(e);
            } 
            catch (NoSuchMethodException e)
            {
                throw new SAXException(e);
            } 
            catch (InstantiationException e)
            {
                throw new SAXException(e);
            } 
            catch (IllegalAccessException e)
            {
                throw new SAXException(e);
            } 
            catch (InvocationTargetException e)
            {
                throw new SAXException(e);
            }

        }
        }
    }    

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException
    {
        switch(m_state)
        {
        case STATE_INITIAL:
            break;
        case STATE_COMMAND_LIST:
            m_state = STATE_INITIAL;
            break;
        case STATE_CATEGORY:
            m_state = STATE_COMMAND_LIST;
            break;
        case STATE_COMMAND:
            m_state = STATE_CATEGORY;
            break;
        }
    }
    
    public List getCommands()
    {
        return m_commands;
    }
    
    public Map getCategories()
    {
        return m_categories;
    }
}
