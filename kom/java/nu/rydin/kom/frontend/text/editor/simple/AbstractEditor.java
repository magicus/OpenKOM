/*
 * Created on Jun 19, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.editor.simple;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.EventDeliveredException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.exceptions.LineOverflowException;
import nu.rydin.kom.exceptions.LineUnderflowException;
import nu.rydin.kom.exceptions.StopCharException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.MessageEditor;
import nu.rydin.kom.frontend.text.editor.Buffer;
import nu.rydin.kom.frontend.text.editor.EditorContext;
import nu.rydin.kom.frontend.text.editor.WordWrapper;
import nu.rydin.kom.frontend.text.parser.Parser;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.utils.PrintUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public abstract class AbstractEditor implements MessageEditor
{	
	private Parser m_parser;
	private final String m_commandList;
	protected final EditorContext m_context; 
	
	public AbstractEditor(String commandList, Context context)
	throws IOException, UnexpectedException
	{
		m_commandList = commandList;
		m_context 	  = new EditorContext(context);
	}
	
	public abstract UnstoredMessage edit(long replyTo)
	throws KOMException, InterruptedException, IOException;
	
	public void fill(String content)
	{
	    m_context.getBuffer().fill(m_context.getWordWrapper(content));
	}
	
	public void fill(WordWrapper wrapper)
	{
	    m_context.getBuffer().fill(wrapper);
	}
	
	protected boolean mainloop(boolean stopOnEmpty)
	throws InterruptedException, UnexpectedException, IOException
	{
	    if(m_parser == null)
	        m_parser = Parser.load(m_commandList, m_context);
	    
		// Set up some stuff
		//
		DisplayController dc = m_context.getDisplayController();
		PrintWriter out = m_context.getOut();
		LineEditor in = m_context.getIn();
		MessageFormatter formatter = m_context.getMessageFormatter();
		Buffer buffer = m_context.getBuffer();
		int width = m_context.getTerminalSettings().getWidth() - 5;
		
		// Mainloop
		//
		String defaultLine = ""; 
		for(;;)
		{
			dc.messageHeader();
			PrintUtils.printRightJustified(out, Integer.toString(buffer.size() + 1), 4);
			out.print(':');
			dc.input();
			out.flush();
			String line = null;
			try
			{
				// TODO: Handle chat messages n'stuff.
				//
				 line = in.readLine(defaultLine, "\u0003\u001a\u0004", width,
				 	LineEditor.FLAG_ECHO | LineEditor.FLAG_STOP_ON_BOL | LineEditor.FLAG_STOP_ON_EOL);
				 	
				 // Check if we got a command
				 //
				 if(line.length() > 0 && line.charAt(0) == '!')
				 {
				 	// Could be a command, but stuff starting with "!!" are 
				 	// escaped "!".
				 	//
				 	if(line.startsWith("!!"))
				 		line = line.substring(1);
				 	else
				 	{
				 		try
				 		{
					 		// It's a command! How great! Go parse it!
					 		//
					 		line = line.substring(1);
					 		Parser.ExecutableCommand executableCommand = null;
					 		executableCommand = m_parser.parseCommandLine(m_context, line);
					 		
					 		if(executableCommand == null)
					 			continue;
					 			
					 		// We have a command. Go run it! ...with two exceptions: The quit
					 		// and the save command. Check them first.
					 		//
					 		if(executableCommand.getCommand().getClass() == Save.class)
					 			return true;
					 		if(executableCommand.getCommand().getClass() == Quit.class)
					 			return false;
				 			executableCommand.execute(m_context);
				 		}
				 		catch(KOMException e)
				 		{
				 			// TODO: Is this the way we should handle this?
				 			//
				 			out.println(e.formatMessage(m_context));
				 		}
				 		
				 		// Don't include this in the buffer!
				 		// 
				 		continue;
				 	}	
				 }
				 
				 // Stop on empty line if requested
				 //
				 if(stopOnEmpty && line.length() == 0)
				     return true;
				 	
				 // Add line to buffer
				 //
				 line += '\n';
				 buffer.add(line);
				 defaultLine = null;
			}
			catch(EventDeliveredException e)
			{
				// TODO: Handle chat messages here!
			}
			catch(LineOverflowException e)
			{
				// Overflow! We have to wrap the line
				//
				String original = e.getLine();
				WordWrapper wrapper = m_context.getWordWrapper(original, width - 1);
				line = wrapper.nextLine();
				buffer.add(line);
				defaultLine = wrapper.nextLine();
				if(defaultLine == null)
					defaultLine = "";
				
				// Erase wrapped portion
				//
				int top = defaultLine.length();
				for(int idx = 0; idx < top; ++idx)
					out.print("\b \b");
				out.println();
			}
			catch(LineUnderflowException e)
			{
				if(buffer.size() > 0)
				{
					defaultLine = buffer.get(buffer.size() - 1).toString();
					buffer.remove(buffer.size() - 1);
				}
				out.println();
			}
			catch(StopCharException e)
			{
				String s = null;
				int i = (int) e.getStopChar();
				switch(e.getStopChar())
				{
					case '\u0004': // Ctrl-D
					case '\u001a': // Ctrl-Z
						s = e.getLine();
						if(s.length() > 0)
							buffer.add(s);
						return true;
					case '\u0003': // Ctrl-C
						return false;
				}
			}				
		}
	}
}
