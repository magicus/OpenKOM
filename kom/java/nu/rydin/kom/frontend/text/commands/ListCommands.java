/*
 * Created on 2004-aug-19
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;
import java.io.PrintWriter;

import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.ClientSession;
import nu.rydin.kom.frontend.text.Command;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;


public class ListCommands extends AbstractCommand
{
	public ListCommands(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);
	}
	
	protected Command[] getCommandList(Context context) throws IOException, KOMException {
		Command[] cmds = ((ClientSession) context).getCommandList();
		return cmds;
	}

	public void execute(Context context, Object[] parameterArray)
            throws KOMException, IOException {
		PrintWriter out = context.getOut();
		Command[] cmds = getCommandList(context);
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