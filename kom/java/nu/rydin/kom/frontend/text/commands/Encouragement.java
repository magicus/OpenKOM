/*
 * Created on Nov 7, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;
import java.util.Random;

import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class Encouragement extends AbstractCommand
{
	public Encouragement(Context context, String fullName)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS);	
	}

	public void execute(Context context, Object[] parameterArray)
	{
	    Random rand = new Random();
	    //Note: Probability of getting the specified upper int is *very* low, 
	    //that's why the last entry is duplicated.
	    int i = rand.nextInt(6) + 1;
	    PrintWriter out = context.getOut();
	    out.println(context.getMessageFormatter().format("encouragement.text" + i));
	}
}
