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
import nu.rydin.kom.exceptions.LineEditingInterruptedException;
import nu.rydin.kom.exceptions.LineOverflowException;
import nu.rydin.kom.exceptions.LineUnderflowException;
import nu.rydin.kom.exceptions.OperationInterruptedException;
import nu.rydin.kom.exceptions.OutputInterruptedException;
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
public abstract class AbstractEditor
{	
    public final static String MESSAGE_EDITOR_STOP_CHARS = "\u000c\u001a\u0004";
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
	
	protected abstract void refresh() throws KOMException;
	
	protected void mainloop(boolean stopOnEmpty)
	throws InterruptedException, OperationInterruptedException, UnexpectedException, IOException
	{
	    if(m_parser == null)
	        m_parser = Parser.load(m_commandList, m_context);
	    
		// Set up some stuff
		//
		DisplayController dc = m_context.getDisplayController();
		PrintWriter out = m_context.getOut();
		LineEditor in = m_context.getIn();
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
			    int flags = LineEditor.FLAG_ECHO | LineEditor.FLAG_STOP_ON_EOL;
			    if(buffer.size() > 0)
			        flags |= LineEditor.FLAG_STOP_ON_BOL;
				 line = in.readLine(defaultLine, MESSAGE_EDITOR_STOP_CHARS, width, flags);
				 	
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
					 			
					 		// We have a command. Go run it! 
					 		//
				 			executableCommand.execute(m_context);
				 		}
				 		catch(OperationInterruptedException e) {
				 		    // ignore it, it was just the command that was interrupted,
				 		    // (like a help listing), not the editing.
				 		}
				 		catch(OutputInterruptedException e) {
				 		    // ignore it, it was just the command that was interrupted,
				 		    // (like a help listing), not the editing.
				 		}
						catch(SaveEditorException e)
						{
						    // We're done. Just return.
						    return;
						}
				 		catch(QuitEditorException e)
				 		{
				 		    throw new LineEditingInterruptedException(null);
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
				     return;
				 	
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
			catch(LineEditingInterruptedException e)
			{
			    defaultLine = e.getPartialLine();
			    if (defaultLine != null) {
			        // If it's null we've gotten here by a Quit command, otherwise by ctrl-c.
			        // Only add extra line in ctrl-c case. (Sorry för fulkoden. /Ihse)
			        out.println();
			    }
			    MessageFormatter formatter = m_context.getMessageFormatter();
			    out.print(formatter.format("simple.editor.abortquestion"));
			    out.flush();
			    String answer = in.readLine();
			    if (answer.equals(formatter.format("misc.y"))) {
			        throw e;
			    }
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
				
				// Erase wrapped portion, but don't erase the character around which we wrap.
				//
				int top = defaultLine.length();
				for(int idx = 1; idx < top; ++idx)
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
				switch(e.getStopChar())
				{
					case '\u0004': // Ctrl-D
					case '\u001a': // Ctrl-Z
					    out.println();
						s = e.getLine();
						if(s.length() > 0)
							buffer.add(s);
						return;
					case '\u000c': // Ctrl-L
					    try
					    {
							s = e.getLine();
							defaultLine = s;
							out.println();
					        refresh();
					    }
					    catch(KOMException e1)
					    {
					        throw new RuntimeException(e1);
					    }
					    break;
				}
			}
		}
	}
}
