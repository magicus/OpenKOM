/*
 * Created on Nov 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.parser;

import java.util.LinkedList;
import java.util.List;

import nu.rydin.kom.frontend.text.Command;

/**
 * @author Pontus Rydin
 */
public class CommandCategory
{
    private String id;
    
    private String i18nKey;
    
    private int order;
    
    private LinkedList commands = new LinkedList();
        
    public CommandCategory(String id, String key, int order)
    {
        super();
        this.id = id;
        i18nKey = key;
        this.order = order;
    }
    public String getI18nKey()
    {
        return i18nKey;
    }
    public String getId()
    {
        return id;
    }
    public int getOrder()
    {
        return order;
    }
    public List getCommands()
    {
        return commands;
    }
    public void addCommand(Command command)
    {
        commands.add(command);
    }
}
