/*
 * Created on Aug 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org/ for details.
 */
package nu.rydin.kom.frontend.text.commands;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.OtherCommandParameter;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.exceptions.CommandNotFoundException;
import nu.rydin.kom.utils.PrintUtils;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.CharArrayWriter;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */

public class Help extends AbstractCommand
{
	//  This class is just a container for the info pulled from
	//  the XML document. It basically does nothing.
	//
    private class HelpTextContainer
	{
		 private String[] paramDescriptions; 
		 private ArrayList description;
		 private ArrayList exceptions;
		 private ArrayList seeAlso;
		 
		 public HelpTextContainer ()
		 {
		     paramDescriptions = new String[10];
		     description = new ArrayList();
		     exceptions = new ArrayList();
		     seeAlso = new ArrayList();
		 }
		 
		 public void addParameterDescription (int n, String s)
		 {
		     paramDescriptions[n] = s;
		 }
		 
		 public String[] getParameterDescriptions()
		 {
		     return paramDescriptions;
		 }
		 
		 public void addDescriptionBlock (String s)
		 {
		     description.add (s.trim());
		 }
		 
		 public ArrayList getDescription()
		 {
		     return description;
		 }
		 
		 public void addExceptionBlock(String s)
		 {
		     exceptions.add(s);
		 }
		 
		 public ArrayList getExceptions()
		 {
		     return exceptions;
		 }
		 
		 public void addReference(String s, String t)
		 {
		     seeAlso.add (new String[] {s, t});
		 }
		 
		 public ArrayList getReferences()
		 {
		     return seeAlso;
		 }
	 }
	
	//  This is a handler class, which receives notifications from the SAX
	//  parser. It will instantiate a HelpTextContainer class, which can later
	//  be retrieved for printing.
    //
	private class HelpDocumentHandler extends DefaultHandler
	{
	     private String m_commandName;
	     private int m_state = -1;
	     private int m_lastParam = -1; 
	     private boolean m_found = false;
	     private HelpTextContainer myContainer = null;
	     private CharArrayWriter contents = new CharArrayWriter();
	     
	     public HelpDocumentHandler (String commandName)
	     {
	         m_commandName = commandName;
	     }
	     
	     public HelpTextContainer getContainer()
	     {
	         return myContainer;
	     }
	     
	     public void startElement (String namespaceURI, String localName, String qName, Attributes attr)
	     throws SAXException
	     {
	         contents.reset();
	         if ("commands".equals(qName))
	         {
	     	    // Entering the outer layer
	     	    //
	     	    m_state = 0;
	     	    return;
	         }
	
	     	if ("command".equals(qName))
	     	{
	     	    // New command
	     	    //
	     	    m_state = 1;
	     	    if (attr.getValue("", "class").equals(m_commandName))
	     	    {
	     	        m_found = true;
	     	        myContainer = new HelpTextContainer ();
	     	    }
	     	    else
	     	    {
	     	        m_found = false;
	     	    }
	         	return;
	     	}
	         	
	     	if ("parameters".equals(qName))
	     	{
	     	    // We've just entered the parameter block
	     	    //
	     	    m_state = 2;
	     	    return;
	     	}
	         	
	     	if ("param".equals(qName))
	     	{
	     	    // A parameter definition
	     	    //
	     	    m_state = 3;
	     	    if (m_found)
	     	    {
	     	        m_lastParam = Integer.parseInt(attr.getValue("", "order"));
	     	    }
	     	    return;
	     	}
	         	
	     	if ("description".equals(qName))
	     	{
	     	    // Entering the description block
	     	    //
	     	    m_state = 4;
	     	    return;
	     	}
	         	
	     	if ("exception".equals(qName))
	     	{
	     	    // Entering the exception block
	     	    //
	     	    m_state = 5;
	     	    return;
	     	}
	 	    
	     	if("seealso".equals(qName))
	     	{
	     	    // Entering the reference block
	     	    //
	     	    m_state = 6;
	     	    return;
	     	}
	         	
	     	if("element".equals(qName))
	     	{
	     	    // A reference element
	     	    //
	     	    m_state = 7;
	     	    if (m_found)
	     	    {
	     	        myContainer.addReference (attr.getValue("", "value"), attr.getValue("", "type"));
	     	    }
	     	    return;
	     	}
	     }
	
	     public void endElement (String namespaceURI, String localName, String qName)
	     throws SAXException
	     {
	         if (m_found)
	         {
	         	if ("block".equals(qName))
	         	{
	 	    	    // A block of text, which is part of either the description
	 	    	    // or the exception block. The state must not be updated as
	 	    	    // we encounter a block, since we need it to place the block
	 	    	    // correctly.
	 	    	    //
	     	        switch (m_state)
	     	        {
	     	        	case 4:
	     	        	    myContainer.addDescriptionBlock(contents.toString());
	     	        	    break;
	     	        	case 5:
	     	        	    myContainer.addExceptionBlock(contents.toString());
	     	        	    break;
	     	        	default:
	     	        	    break;
	     	        }
	     	    }
	         	if ("param".equals(qName))
	         	{
	         	    myContainer.addParameterDescription(m_lastParam, contents.toString());
	         	}
	     	    return;
	     	}
	     }
	     
	     public void characters (char[] ch, int start, int length)
	     throws SAXException 
	     {
	         contents.write (ch, start, length);
	     }
	}

	public Help (Context context, String fullName)
	{
		super (fullName, new CommandLineParameter[] {new OtherCommandParameter ("", false)});	
	}
	
	public void execute (Context context, Object[] parameterArray)
	throws UnexpectedException, CommandNotFoundException
	{
        Command command = (Command) parameterArray[0];
        if (null == command)
        {
            command = this;
        }
        
	    HelpDocumentHandler myParser = new HelpDocumentHandler (command.getFullName());
	    HelpTextContainer htc = null;
	    
	    try
	    {
	        XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
	        xr.setContentHandler(myParser);
	        xr.parse (new InputSource (ClassLoader.getSystemClassLoader().
	                getResourceAsStream("nu/rydin/kom/i18n/help.xml")));
	        htc = myParser.getContainer();
	    }
	    catch (Exception e)
	    {
	        throw new UnexpectedException(context.getSession().getLoggedInUserId(), e);
	    }
	    	    
	    // OK, so now we have a container containing the help text.
	    // Time to format and print it.
	    //
	    PrintWriter out = context.getOut();
	    MessageFormatter formatter = context.getMessageFormatter();

	    if (null == htc)
	    {
	        out.println (formatter.format("help.undocumented"));
	        return;
	    }

	    int termWidth = context.getTerminalSettings().getWidth();
	    out.println (formatter.format("help.commandname"));
	    out.print ("  " + command.getFullName());
	    CommandLineParameter[] clpa = command.getSignature();
	    for (int i = 0; i < clpa.length; ++i)
	    {
	        // TODO: Sluta hota älgbeståndet här.
	        //
	        out.print ((0 < i ? new String(new char[] { clpa[i].getSeparator(), ' ' }) : " ") + clpa[i].getUserDescription(context));
	    }
	    out.println();	    
	    out.println();

	    if (0 < clpa.length)
	    {
		    out.println (formatter.format("help.parameters"));
		    String[] pDesc = htc.getParameterDescriptions();
		    
		    for (int i = 0; i < clpa.length; ++i)
		    {
		        PrintUtils.printIndented (out, clpa[i].getUserDescription(context) + ": " + (null == pDesc[i] ? "" : pDesc[i]), termWidth, "  ");
		    }
		    out.println();
	    }

	    out.println (formatter.format("help.description"));
	    ArrayList myDesc = htc.getDescription();
	    for (int i = 0; i < myDesc.size(); ++i)
	    {
	        PrintUtils.printIndented(out, myDesc.get(i).toString(), termWidth, "  ");
	        out.println();
	    }

	    ArrayList myExc = htc.getExceptions();
	    if (0 < myExc.size())
	    {
		    out.println (formatter.format("help.exceptions"));
		    for (int i = 0; i < myExc.size(); ++i)
		    {
			    PrintUtils.printIndented(out, myExc.get(i).toString(), termWidth, "  ");
			    out.println();
		    }
	    }
	    
	    ArrayList myRefs = htc.getReferences();
	    if (0 < myRefs.size())
	    {
		    out.println (formatter.format("help.seealso"));
		    for (int i = 0; i < htc.getReferences().size(); ++i)
		    {
		        String[] p = (String[]) htc.getReferences().get(i);
		        if (p[1].equals("command"))
	                out.println ("  " + formatter.format(p[0] + ".name"));		  
		        else
		            out.println ("  " + p[0]);
		    }
	    }
	}
}