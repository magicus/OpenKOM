/*
 * Created on 2004-aug-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;


public class ListCommands extends AbstractCommand
{
	public ListCommands(String fullName)
	{
		super(fullName);
	}
	
	public void execute(Context context, String[] args)
	{
	}
	
    public void execute2(Context context, Object[] parameterArray)
            throws KOMException, IOException, InterruptedException {
		PrintWriter out = context.getOut();
		Command[] cmds = context.getClientSession().getCommandList();
		int top = cmds.length;
		for(int idx = 0; idx < top; ++idx) {
			out.print(cmds[idx].getFullName());
			CommandLineParameter[] parameters = cmds[idx].getSignature();
			for (int j = 0; j < parameters.length; j++) {
                CommandLineParameter parameter = parameters[j];
                if (j > 0) {
                    out.print(parameter.getSeparator());
                }
                out.print(" " + parameter.getUserDescription(context));
            }
			out.println();
		}
    }
}