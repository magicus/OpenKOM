/*
 * Created on Aug 25, 2004
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.NamedObjectParameter;
import nu.rydin.kom.frontend.text.parser.RawParameter;
import nu.rydin.kom.structs.NameAssociation;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 */
public class ShowFile extends AbstractCommand
{
	public ShowFile(Context context, String fullname)
	{
		super(fullname, new CommandLineParameter[] { 
		        new RawParameter("edit.file.prompt", true),
		        new NamedObjectParameter(false)});
	}

    public void execute(Context context, Object[] parameters)
            throws KOMException, IOException, InterruptedException
    {
        ServerSession session = context.getSession();
        PrintWriter out = context.getOut();
        
        // Extract data from parameters
        //
        String fileName = (String) parameters[0];
        long parent = parameters[1] != null
    	? ((NameAssociation) parameters[1]).getId()
    	        : context.getSession().getCurrentConferenceId();

        // Show file
        //
        BufferedReader rdr = new BufferedReader(new StringReader(
                session.readFile(parent, fileName)));
        String line;
        while((line = rdr.readLine()) != null)
            out.println(line);
    }

}
