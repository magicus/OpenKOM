/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.frontend.text.commands;

import java.io.PrintWriter;
import java.util.HashMap;

import nu.rydin.kom.constants.Activities;
import nu.rydin.kom.constants.UserFlags;
import nu.rydin.kom.exceptions.KOMException;
import nu.rydin.kom.frontend.text.AbstractCommand;
import nu.rydin.kom.frontend.text.Context;
import nu.rydin.kom.frontend.text.DisplayController;
import nu.rydin.kom.i18n.MessageFormatter;
import nu.rydin.kom.structs.UserListItem;
import nu.rydin.kom.utils.HeaderPrinter;
import nu.rydin.kom.utils.PrintUtils;
import nu.rydin.kom.utils.StringUtils;

/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 */
public class Who extends AbstractCommand
{

	public Who(Context context, String fullName, long permissions)
	{
		super(fullName, AbstractCommand.NO_PARAMETERS, permissions);	
	}
	
    public void execute(Context context, Object[] parameterArray)
            throws KOMException
    {
		MessageFormatter formatter = context.getMessageFormatter();
		PrintWriter out = context.getOut();
		UserListItem[] users = context.getSession().listLoggedInUsers();

		// Compact if user wants it
    	//
    	if(context.getCachedUserInfo().testFlags(0, UserFlags.COMPACT_WHO))
    		this.makeCompact(users);
		DisplayController dc = context.getDisplayController();
		dc.normal();
		HeaderPrinter hp = new HeaderPrinter();
		hp.addHeader(formatter.format("who.login"), 7, true);
		hp.addHeader(formatter.format("who.idle"), 7, true);
		hp.addSpace(1);
		int termWidth = context.getTerminalSettings().getWidth();
		int firstColsWidth = 7 + 7 + 1;
		int lastColWidth = termWidth - firstColsWidth - 1 ; 
		hp.addHeader(formatter.format("who.name"), lastColWidth, false);
		hp.printOn(out);
		int top = users.length;
		dc.output();
		int active = 0;
		for(int idx = 0; idx < top; ++idx)
		{
			UserListItem each = users[idx];
			if(each == null)
				continue; // Eliminated by compacting
			String confName = each.isInMailbox() 
				? formatter.format("misc.mailboxtitle")
				: context.formatObjectName(each.getConference());
			long now = System.currentTimeMillis();
			PrintUtils.printRightJustified(out, StringUtils.formatElapsedTime(now - each.getLoginTime()), 7);
			long idle = now - each.getLastHeartbeat();
			PrintUtils.printRightJustified(out, idle >= 60000 ? StringUtils.formatElapsedTime(now - each.getLastHeartbeat()) : "", 7);
            
            out.print(' ');
            // Depending on the activity, format a suitable string and print it.
            //
            switch (each.getActivity())
            {
            case Activities.AUTO:               // OpenKOM standard, shows conference.
    			PrintUtils.printIndented(out, 
    			        formatter.format("who.format", new Object[] { context.formatObjectName(each.getUser()), confName }),
    			        lastColWidth, 0, firstColsWidth);
                break;

            case Activities.MAIL:               // User is writing a mail
                PrintUtils.printIndented(out,
                        formatter.format(each.getLastObject() == context.getSession().getLoggedInUserId() ?
                                         "who.mail.to.you" : "who.mail", new Object[] { context.formatObjectName(each.getUser()) }),
                        lastColWidth, 0, firstColsWidth);
                break;

            case Activities.POST:               // User is writing a post or a reply
                PrintUtils.printIndented(out, 
                        formatter.format("who.post.where", new Object[] { context.formatObjectName(each.getUser()), confName }),
                        lastColWidth, 0, firstColsWidth);
                break;

            case Activities.CHAT:               // User is sending a unicast or multicast
                PrintUtils.printIndented(out,
                        formatter.format(each.getLastObject() == context.getSession().getLoggedInUserId() ?
                                         "who.chat.to.you" : "who.chat", new Object[] { context.formatObjectName(each.getUser()) }),
                        lastColWidth, 0, firstColsWidth);
                break;
                
            case Activities.BROADCAST:          // User is sending a broadcast message
                PrintUtils.printIndented(out, 
                        formatter.format("who.broadcast", new Object[] { context.formatObjectName(each.getUser()) }),
                        lastColWidth, 0, firstColsWidth);
                break;

            case Activities.FREETEXT:           // User has set his or her own activity
                PrintUtils.printIndented(out,
                        formatter.format("who.freeformat", new Object[] { context.formatObjectName(each.getUser()), each.getActivityText() }),
                        lastColWidth, 0, firstColsWidth);
                break;

            default:                            // Can't happen. We think.
                PrintUtils.printIndented(out,
                        "Strange state.",
                        lastColWidth, 0, firstColsWidth);
                break;
            };
			if(idle < 60000)
			    ++active;
		}
		out.println();
		out.println(formatter.format("who.total", new Object[] { new Integer(top), new Integer(active) }));
    }
    
    private void makeCompact(UserListItem[] users)
    {
    	int top = users.length;
    	HashMap<Long, Integer> index = new HashMap<Long, Integer>(top);
    	for(int idx = 0; idx < top; ++idx)
    	{
    		UserListItem each = users[idx];
    		long id = each.getUser().getId();
    		
    		// Haven't seen it before? Remember it and skip to next!
    		//
    		if(!index.containsKey(id))
    		{
    			index.put(id, idx);
    			continue;
    		}
    		
    		// We've seen it before. Consolidate!
    		//
    		int p = index.get(id);
    		UserListItem old = users[p];
    		boolean moreActive = each.getLastHeartbeat() > old.getLastHeartbeat();
    		users[p] = new UserListItem(old.getSessionId(), old.getUser(), old.getClientType(), 
    					(moreActive ? each.getAction() : old.getAction()),
    					(moreActive ? each.getConference() : old.getConference()),
    					(moreActive ? each.isInMailbox() : old.isInMailbox()), 
    					Math.min(old.getLoginTime(), each.getLoginTime()),
    					Math.max(old.getLastHeartbeat(), each.getLastHeartbeat()),
                        each.getActivity(), each.getActivityText(), each.getLastObject());
    		users[idx] = null;
    	}
    }
}
