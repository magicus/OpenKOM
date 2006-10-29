/*
 * Created on Sep 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.IOException;

import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.KOMWriter;
import nu.rydin.kom.frontend.text.LineEditor;
import nu.rydin.kom.frontend.text.parser.CommandLineParameter;
import nu.rydin.kom.frontend.text.parser.ConferenceParameter;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.ConferenceType;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.utils.ConferenceUtils;

/**
 * @author Pontus Rydin
 */
public class ChangeConferenceParameters extends AbstractCommand
{
	public ChangeConferenceParameters(Context context, String fullName, long permissions)
	{
		super(fullName, new CommandLineParameter[] { new ConferenceParameter(true) }, permissions);	
	}
	
    public void execute(Context context, Object[] parameterArray) 
	throws KOMException, IOException, InterruptedException, DuplicateNameException
	{
        ServerSession session = context.getSession();
        KOMWriter out = context.getOut();
        LineEditor in = context.getIn();
        MessageFormatter fmt = context.getMessageFormatter();
        
        // Load conference
        //
        NameAssociation conference = (NameAssociation) parameterArray[0];
        ConferenceInfo ci = session.getConference(conference.getId());
                
        // Go ask user for changed parameters
        //
        ConferenceType ct = ConferenceUtils.askForConferenceType(context,
                ci.getPermissions(), ci.getNonmemberPermissions(), conference.getName().getVisibility());
        
        // Update conference
        //
        session.updateConferencePermissions(conference.getId(),
                ct.getPermissions(), ct.getNonMemberPermissions(),
                ct.getVisibility());
        
        // Print confirmation
        //
        context.getOut().println(fmt.format("change.conference.parameters.confirm", 
                conference.getName().toString()));
	}
}
