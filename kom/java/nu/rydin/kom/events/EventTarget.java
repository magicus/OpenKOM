/*
 * Created on Nov 11, 2003
 *
 * Distributed under the GPL licens.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.events;


/**
 * @author <a href=mailto:pontus@rydin.nu>Pontus Rydin</a>
 * @author <a href=mailto:jepson@xyzzy.se>Jepson</a>
 * @author <a href=mailto:guru@slideware.com>fuffenz</a>
 */
public interface EventTarget
{
	public void onEvent(Event event);
	
	public void onEvent(ChatMessageEvent event);
	
	public void onEvent(BroadcastMessageEvent event); 
	
	public void onEvent(ChatAnonymousMessageEvent event); 

	public void onEvent(BroadcastAnonymousMessageEvent event); 

	public void onEvent(NewMessageEvent event);
	
	public void onEvent(UserAttendanceEvent event);
	
	public void onEvent(ReloadUserProfileEvent event);
	
	public void onEvent(ReevaluateDefaultEvent event);
}
