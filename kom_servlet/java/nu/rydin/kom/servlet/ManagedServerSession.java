package nu.rydin.kom.servlet;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import nu.rydin.kom.backend.EventSource;
import nu.rydin.kom.backend.HeartbeatListener;
import nu.rydin.kom.backend.ServerSession;
import nu.rydin.kom.events.Event;
import nu.rydin.kom.exceptions.AllRecipientsNotReachedException;
import nu.rydin.kom.exceptions.AlreadyMemberException;
import nu.rydin.kom.exceptions.AmbiguousNameException;
import nu.rydin.kom.exceptions.AuthorizationException;
import nu.rydin.kom.exceptions.BadPasswordException;
import nu.rydin.kom.exceptions.DuplicateNameException;
import nu.rydin.kom.exceptions.NoCurrentMessageException;
import nu.rydin.kom.exceptions.NoMoreMessagesException;
import nu.rydin.kom.exceptions.NoMoreNewsException;
import nu.rydin.kom.exceptions.NoRulesException;
import nu.rydin.kom.exceptions.NotAReplyException;
import nu.rydin.kom.exceptions.NotLoggedInException;
import nu.rydin.kom.exceptions.NotMemberException;
import nu.rydin.kom.exceptions.ObjectNotFoundException;
import nu.rydin.kom.exceptions.SelectionOverflowException;
import nu.rydin.kom.exceptions.UnexpectedException;
import nu.rydin.kom.structs.Bookmark;
import nu.rydin.kom.structs.ConferenceInfo;
import nu.rydin.kom.structs.ConferenceListItem;
import nu.rydin.kom.structs.ConferencePermission;
import nu.rydin.kom.structs.Envelope;
import nu.rydin.kom.structs.FileStatus;
import nu.rydin.kom.structs.GlobalMessageSearchResult;
import nu.rydin.kom.structs.LocalMessageSearchResult;
import nu.rydin.kom.structs.MembershipInfo;
import nu.rydin.kom.structs.MembershipListItem;
import nu.rydin.kom.structs.MessageHeader;
import nu.rydin.kom.structs.MessageLocator;
import nu.rydin.kom.structs.MessageLogItem;
import nu.rydin.kom.structs.MessageOccurrence;
import nu.rydin.kom.structs.MessageSearchResult;
import nu.rydin.kom.structs.Name;
import nu.rydin.kom.structs.NameAssociation;
import nu.rydin.kom.structs.NamedObject;
import nu.rydin.kom.structs.Relationship;
import nu.rydin.kom.structs.SessionState;
import nu.rydin.kom.structs.SystemInformation;
import nu.rydin.kom.structs.UnstoredMessage;
import nu.rydin.kom.structs.UserInfo;
import nu.rydin.kom.structs.UserListItem;
import nu.rydin.kom.structs.UserLogItem;
import nu.rydin.kom.utils.Logger;
import nu.rydin.kom.utils.PasswordUtils;

public class ManagedServerSession implements HttpSessionBindingListener, ServerSession
{
    private final KOMSessionFactory factory;
    private final ServerSession underlying;
    private final String passwordDigest;
    private int refCount = 0;
    
    public ManagedServerSession(KOMSessionFactory factory, ServerSession underlying, String passwordDigest)
    {
        this.factory = factory;
        this.underlying = underlying;
        this.passwordDigest = passwordDigest;
    }
    
    public boolean authenticate(String password)
    {
        try
        {
            return PasswordUtils.compareDigest(password, passwordDigest);
        }
        catch(NoSuchAlgorithmException e)
        {
            // Just log and report failure
            //
            Logger.fatal(this, "No encryption algorithm. Cannot authenticate!", e);
            return false;
        }
    }
    
    public void valueBound(HttpSessionBindingEvent event)
    {
        // Not much to do
    }

    public void valueUnbound(HttpSessionBindingEvent event)
    {
        try
        {
            Logger.info(this, "Received valueUnbound request on a ServerSession");
            this.release();
        }
        catch(UnexpectedException e)
        {
            // Not much we can do..
            //
            Logger.error(this, "Error when dropping session", e);
        }
    }
    
    public synchronized void grab()
    {
        ++refCount;
        Logger.info(this, "ManagedServer session: Refcount increased to " + refCount);
    }
    
    private synchronized void release()
    throws UnexpectedException
    {
        if(--refCount == 0)
        {
            underlying.close();
            factory.releaseSession(this);
        }
        Logger.info(this, "ManagedServer session: Refcount decreased to " + refCount);
    }

    public void addMessageAttribute(long message, short attribute, String payload, boolean deleteOld) throws UnexpectedException, AuthorizationException
    {
        underlying.addMessageAttribute(message, attribute, payload, deleteOld);
    }

    public void allowLogin() throws AuthorizationException
    {
        underlying.allowLogin();
    }

    public void assertConferencePermission(long conferenceId, int mask) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.assertConferencePermission(conferenceId, mask);
    }

    public void assertModifyConference(long conferenceId) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.assertModifyConference(conferenceId);
    }

    public NameAssociation[] broadcastChatMessage(String message, short kind) throws UnexpectedException
    {
        return underlying.broadcastChatMessage(message, kind);
    }

    public boolean canManipulateObject(long object) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.canManipulateObject(object);
    }

    public void changeContactInfo(UserInfo ui) throws ObjectNotFoundException, UnexpectedException, AuthorizationException
    {
        underlying.changeContactInfo(ui);
    }

    public void changePassword(long userId, String oldPassword, String newPassword) throws ObjectNotFoundException, AuthorizationException, UnexpectedException, BadPasswordException
    {
        underlying.changePassword(userId, oldPassword, newPassword);
    }

    public void changeReplyToConference(long originalConferenceId, long newReplyToConferenceId) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.changeReplyToConference(originalConferenceId, newReplyToConferenceId);
    }

    public void changeSetting(String name, long value) throws AuthorizationException, UnexpectedException
    {
        underlying.changeSetting(name, value);
    }

    public void changeSetting(String name, String value) throws AuthorizationException, UnexpectedException
    {
        underlying.changeSetting(name, value);
    }

    public void changeSuffixOfLoggedInUser(String suffix) throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        underlying.changeSuffixOfLoggedInUser(suffix);
    }

    public void changeSuffixOfUser(long id, String suffix) throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        underlying.changeSuffixOfUser(id, suffix);
    }

    public void changeUnread(int nUnread) throws ObjectNotFoundException, UnexpectedException
    {
        underlying.changeUnread(nUnread);
    }

    public void changeUserFlags(long[] set, long[] reset) throws ObjectNotFoundException, UnexpectedException
    {
        underlying.changeUserFlags(set, reset);
    }

    public void changeUserPermissions(long user, long set, long reset) throws ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        underlying.changeUserPermissions(user, set, reset);
    }

    public boolean checkForUserid(String userid) throws UnexpectedException
    {
        return underlying.checkForUserid(userid);
    }

    public void checkRights(long mask) throws AuthorizationException
    {
        underlying.checkRights(mask);
    }

    public void clearCache() throws AuthorizationException
    {
        underlying.clearCache();
    }

    public void close() throws UnexpectedException
    {
        underlying.close();
    }

    public void copyMessage(long globalNum, long conferenceId) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.copyMessage(globalNum, conferenceId);
    }

    public long countAllMessagesLocally(long conference) throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        return underlying.countAllMessagesLocally(conference);
    }

    public long countCommentsGloballyToAuthor(long user, Timestamp startDate) throws UnexpectedException
    {
        return underlying.countCommentsGloballyToAuthor(user, startDate);
    }

    public long countGrepMessagesLocally(long conference, String searchterm) throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        return underlying.countGrepMessagesLocally(conference, searchterm);
    }

    public long countMessagesGloballyByAuthor(long user) throws UnexpectedException
    {
        return underlying.countMessagesGloballyByAuthor(user);
    }

    public long countMessagesLocallyByAuthor(long conference, long user) throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        return underlying.countMessagesLocallyByAuthor(conference, user);
    }

    public long countSearchMessagesGlobally(String searchterm) throws UnexpectedException
    {
        return underlying.countSearchMessagesGlobally(searchterm);
    }

    public long countSearchMessagesLocally(long conference, String searchterm) throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        return underlying.countSearchMessagesLocally(conference, searchterm);
    }

    public int countUnread(long conference) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.countUnread(conference);
    }

    public long createConference(String fullname, String keywords, int permissions, int nonmemberPermissions, short visibility, long replyConf) throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException
    {
        return underlying.createConference(fullname, keywords, permissions, nonmemberPermissions, visibility, replyConf);
    }

    public void createUser(String userid, String password, String fullname, String keywords, String address1, String address2, String address3, String address4, String phoneno1, String phoneno2, String email1, String email2, String url, String charset, long flags1, long flags2, long flags3, long flags4, long rights) throws UnexpectedException, AmbiguousNameException, DuplicateNameException, AuthorizationException
    {
        underlying.createUser(userid, password, fullname, keywords, address1, address2, address3, address4, phoneno1, phoneno2, email1, email2, url, charset, flags1, flags2, flags3, flags4, rights);
    }

    public void createUserFilter(long jinge, long flags) throws ObjectNotFoundException, UnexpectedException
    {
        underlying.createUserFilter(jinge, flags);
    }

    public void deleteConference(long conference) throws AuthorizationException, UnexpectedException
    {
        underlying.deleteConference(conference);
    }

    public void deleteFile(long parent, String name) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.deleteFile(parent, name);
    }

    public void deleteMessage(int localNum, long conference) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.deleteMessage(localNum, conference);
    }

    public void deleteMessageInCurrentConference(int localNum) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.deleteMessageInCurrentConference(localNum);
    }

    public void deleteSystemFile(String name) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.deleteSystemFile(name);
    }

    public void detach() throws UnexpectedException
    {
        underlying.detach();
    }

    public void disableSelfRegistration() throws AuthorizationException, UnexpectedException
    {
        underlying.disableSelfRegistration();
    }

    public void dropUserFilter(long user) throws ObjectNotFoundException, UnexpectedException
    {
        underlying.dropUserFilter(user);
    }

    public void enableSelfRegistration() throws AuthorizationException, UnexpectedException
    {
        underlying.enableSelfRegistration();
    }

    public NameAssociation[] getAssociationsForPattern(String pattern) throws UnexpectedException
    {
        return underlying.getAssociationsForPattern(pattern);
    }

    public NameAssociation[] getAssociationsForPatternAndKind(String pattern, short kind) throws UnexpectedException
    {
        return underlying.getAssociationsForPatternAndKind(pattern, kind);
    }

    public MessageLogItem[] getBroadcastMessagesFromLog(int limit) throws UnexpectedException
    {
        return underlying.getBroadcastMessagesFromLog(limit);
    }

    public MessageLogItem[] getChatMessagesFromLog(int limit) throws UnexpectedException
    {
        return underlying.getChatMessagesFromLog(limit);
    }

    public short getClientType()
    {
        return underlying.getClientType();
    }

    public ConferenceInfo getConference(long conferenceId) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.getConference(conferenceId);
    }

    public ConferenceInfo getCurrentConference()
    {
        return underlying.getCurrentConference();
    }

    public long getCurrentConferenceId()
    {
        return underlying.getCurrentConferenceId();
    }

    public long getCurrentMessage() throws NoCurrentMessageException
    {
        return underlying.getCurrentMessage();
    }

    public MessageOccurrence getCurrentMessageOccurrence() throws NoCurrentMessageException, UnexpectedException
    {
        return underlying.getCurrentMessageOccurrence();
    }

    public String getDebugString()
    {
        return underlying.getDebugString();
    }

    public EventSource getEventSource()
    {
        return underlying.getEventSource();
    }

    public long getGlobalMessageId(MessageLocator textnumber) throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        return underlying.getGlobalMessageId(textnumber);
    }

    public HeartbeatListener getHeartbeatListener()
    {
        return underlying.getHeartbeatListener();
    }

    public long getLastHeartbeat()
    {
        return underlying.getLastHeartbeat();
    }

    public MessageHeader getLastMessageHeader() throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        return underlying.getLastMessageHeader();
    }

    public Envelope getLastRulePosting() throws ObjectNotFoundException, NoRulesException, UnexpectedException
    {
        return underlying.getLastRulePosting();
    }

    public Envelope getLastRulePostingInConference(long conference) throws ObjectNotFoundException, NoRulesException, UnexpectedException
    {
        return underlying.getLastRulePostingInConference(conference);
    }

    public UserInfo getLoggedInUser()
    {
        return underlying.getLoggedInUser();
    }

    public long getLoggedInUserId()
    {
        return underlying.getLoggedInUserId();
    }

    public long getLoginTime()
    {
        return underlying.getLoginTime();
    }

    public MessageHeader getMessageHeader(MessageLocator locator) throws ObjectNotFoundException, AuthorizationException, UnexpectedException, NoCurrentMessageException
    {
        return underlying.getMessageHeader(locator);
    }

    public MessageOccurrence getMostRelevantOccurrence(long conferenceId, long messageId) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.getMostRelevantOccurrence(conferenceId, messageId);
    }

    public MessageLogItem[] getMulticastMessagesFromLog(int limit) throws UnexpectedException
    {
        return underlying.getMulticastMessagesFromLog(limit);
    }

    public Name getName(long id) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.getName(id);
    }

    public NamedObject getNamedObject(long id) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.getNamedObject(id);
    }

    public Name[] getNames(long[] id) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.getNames(id);
    }

    public short getObjectKind(long conference) throws ObjectNotFoundException
    {
        return underlying.getObjectKind(conference);
    }

    public MessageOccurrence getOriginalMessageOccurrence(long messageId) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.getOriginalMessageOccurrence(messageId);
    }

    public int getPermissionsInConference(long conferenceId) throws UnexpectedException, ObjectNotFoundException
    {
        return underlying.getPermissionsInConference(conferenceId);
    }

    public int getPermissionsInCurrentConference() throws UnexpectedException, ObjectNotFoundException
    {
        return underlying.getPermissionsInCurrentConference();
    }

    public int getSessionId()
    {
        return underlying.getSessionId();
    }

    public SessionState getSessionState() throws UnexpectedException
    {
        return underlying.getSessionState();
    }

    public SystemInformation getSystemInformation() throws UnexpectedException
    {
        return underlying.getSystemInformation();
    }

    public UserInfo getUser(long userId) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.getUser(userId);
    }

    public int getUserPermissionsInConference(long userId, long conferenceId) throws UnexpectedException, ObjectNotFoundException
    {
        return underlying.getUserPermissionsInConference(userId, conferenceId);
    }

    public MessageOccurrence globalToLocal(long globalNum) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.globalToLocal(globalNum);
    }

    public MessageOccurrence globalToLocalInConference(long conferenceId, long globalNum) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.globalToLocalInConference(conferenceId, globalNum);
    }

    public void gotoConference(long id) throws UnexpectedException, ObjectNotFoundException, NotMemberException
    {
        underlying.gotoConference(id);
    }

    public long gotoNextConference() throws NoMoreNewsException, UnexpectedException
    {
        return underlying.gotoNextConference();
    }

    public LocalMessageSearchResult[] grepMessagesLocally(long conference, String searchterm, int offset, int length) throws UnexpectedException
    {
        return underlying.grepMessagesLocally(conference, searchterm, offset, length);
    }

    public boolean hasPermissionInConference(long conferenceId, int mask) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        return underlying.hasPermissionInConference(conferenceId, mask);
    }

    public boolean hasPermissionInCurrentConference(int mask) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        return underlying.hasPermissionInCurrentConference(mask);
    }

    public boolean hasSession(long userId)
    {
        return underlying.hasSession(userId);
    }

    public boolean isValid()
    {
        return underlying.isValid();
    }

    public void killAllSessions() throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.killAllSessions();
    }

    public void killSession(int sessionId) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.killSession(sessionId);
    }

    public LocalMessageSearchResult[] listAllMessagesLocally(long conference, int start, int length) throws UnexpectedException
    {
        return underlying.listAllMessagesLocally(conference, start, length);
    }

    public MessageSearchResult[] listCommentsGloballyToAuthor(long user, Timestamp startDate, int offset, int length) throws UnexpectedException
    {
        return underlying.listCommentsGloballyToAuthor(user, startDate, offset, length);
    }

    public MembershipInfo[] listConferenceMembers(long confId) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.listConferenceMembers(confId);
    }

    public ConferencePermission[] listConferencePermissions(long conf) throws UnexpectedException
    {
        return underlying.listConferencePermissions(conf);
    }

    public ConferencePermission[] listConferencePermissionsInCurrentConference() throws UnexpectedException
    {
        return underlying.listConferencePermissionsInCurrentConference();
    }

    public ConferenceListItem[] listConferencesByDate() throws UnexpectedException
    {
        return underlying.listConferencesByDate();
    }

    public ConferenceListItem[] listConferencesByName() throws UnexpectedException
    {
        return underlying.listConferencesByName();
    }

    public FileStatus[] listFiles(long parent, String pattern) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.listFiles(parent, pattern);
    }

    public Relationship[] listFilters() throws UnexpectedException
    {
        return underlying.listFilters();
    }

    public UserListItem[] listLoggedInUsers() throws UnexpectedException
    {
        return underlying.listLoggedInUsers();
    }

    public NameAssociation[] listMembersByConference(long confId) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.listMembersByConference(confId);
    }

    public NameAssociation[] listMemberships(long userId) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.listMemberships(userId);
    }

    public GlobalMessageSearchResult[] listMessagesGloballyByAuthor(long user, int offset, int length) throws UnexpectedException
    {
        return underlying.listMessagesGloballyByAuthor(user, offset, length);
    }

    public LocalMessageSearchResult[] listMessagesLocallyByAuthor(long conference, long user, int start, int length) throws UnexpectedException
    {
        return underlying.listMessagesLocallyByAuthor(conference, user, start, length);
    }

    public MembershipListItem[] listNews() throws UnexpectedException
    {
        return underlying.listNews();
    }

    public MembershipListItem[] listNewsFor(long userId) throws UnexpectedException
    {
        return underlying.listNewsFor(userId);
    }

    public UserLogItem[] listUserLog(long user, Timestamp start, Timestamp end, int offset, int length) throws UnexpectedException
    {
        return underlying.listUserLog(user, start, end, offset, length);
    }

    public UserLogItem[] listUserLog(Timestamp start, Timestamp end, int offset, int length) throws UnexpectedException
    {
        return underlying.listUserLog(start, end, offset, length);
    }

    public long localToGlobal(long conference, int localNum) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.localToGlobal(conference, localNum);
    }

    public long localToGlobalInCurrentConference(int localNum) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.localToGlobalInCurrentConference(localNum);
    }

    public void markAsUnreadAtLogout(MessageLocator message) throws UnexpectedException, ObjectNotFoundException, NoCurrentMessageException
    {
        underlying.markAsUnreadAtLogout(message);
    }

    public int markSubjectAsUnread(String subject, boolean localOnly) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.markSubjectAsUnread(subject, localOnly);
    }

    public int markThreadAsUnread(long root) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.markThreadAsUnread(root);
    }

    public void moveMessage(long messageId, long destConfId) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.moveMessage(messageId, destConfId);
    }

    public void postEvent(Event e)
    {
        underlying.postEvent(e);
    }

    public long prioritizeConference(long conference, long targetconference) throws ObjectNotFoundException, UnexpectedException, NotMemberException
    {
        return underlying.prioritizeConference(conference, targetconference);
    }

    public void prohibitLogin() throws AuthorizationException
    {
        underlying.prohibitLogin();
    }

    public String readFile(long parent, String name) throws ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        return underlying.readFile(parent, name);
    }

    public Envelope readLastMessage() throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        return underlying.readLastMessage();
    }

    public Envelope readNextMessage(long conf) throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException
    {
        return underlying.readNextMessage(conf);
    }

    public Envelope readNextMessageInCurrentConference() throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException
    {
        return underlying.readNextMessageInCurrentConference();
    }

    public Envelope readNextReply() throws NoMoreMessagesException, ObjectNotFoundException, UnexpectedException
    {
        return underlying.readNextReply();
    }

    public Envelope readOriginalMessage() throws NoCurrentMessageException, NotAReplyException, ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        return underlying.readOriginalMessage();
    }

    public String readSystemFile(String name) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        return underlying.readSystemFile(name);
    }

    public Envelope readTaggedMessage(short tag, long object) throws UnexpectedException, ObjectNotFoundException
    {
        return underlying.readTaggedMessage(tag, object);
    }

    public void renameObject(long id, String newName) throws DuplicateNameException, ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        underlying.renameObject(id, newName);
    }

    public void revokeConferencePermissions(long conf, long user) throws UnexpectedException
    {
        underlying.revokeConferencePermissions(conf, user);
    }

    public void revokeConferencePermissionsInCurrentConference(long user) throws UnexpectedException
    {
        underlying.revokeConferencePermissionsInCurrentConference(user);
    }

    public int rollbackReads(int n) throws UnexpectedException
    {
        return underlying.rollbackReads(n);
    }

    public GlobalMessageSearchResult[] searchMessagesGlobally(String searchterm, int offset, int length) throws UnexpectedException
    {
        return underlying.searchMessagesGlobally(searchterm, offset, length);
    }

    public LocalMessageSearchResult[] searchMessagesLocally(long conference, String searchterm, int offset, int length) throws UnexpectedException
    {
        return underlying.searchMessagesLocally(conference, searchterm, offset, length);
    }

    public NameAssociation[] sendMulticastMessage(long[] destinations, String message, boolean logAsMulticast) throws NotLoggedInException, ObjectNotFoundException, AllRecipientsNotReachedException, UnexpectedException
    {
        return underlying.sendMulticastMessage(destinations, message, logAsMulticast);
    }

    public void setConferencePermissions(long conf, long user, int permissions) throws UnexpectedException
    {
        underlying.setConferencePermissions(conf, user, permissions);
    }

    public void setConferencePermissionsInCurrentConference(long user, int permissions) throws UnexpectedException
    {
        underlying.setConferencePermissionsInCurrentConference(user, permissions);
    }

    public void setCurrentConferenceId(long id) throws UnexpectedException, ObjectNotFoundException
    {
        underlying.setCurrentConferenceId(id);
    }

    public Name signoff(long conferenceId) throws ObjectNotFoundException, UnexpectedException, NotMemberException
    {
        return underlying.signoff(conferenceId);
    }

    public Name signup(long conferenceId) throws ObjectNotFoundException, AlreadyMemberException, UnexpectedException, AuthorizationException
    {
        return underlying.signup(conferenceId);
    }

    public int skipBranch(long node) throws UnexpectedException, NoCurrentMessageException, ObjectNotFoundException
    {
        return underlying.skipBranch(node);
    }

    public int skipMessagesBySubject(String subject, boolean skipGlobal) throws UnexpectedException, NoCurrentMessageException, ObjectNotFoundException
    {
        return underlying.skipMessagesBySubject(subject, skipGlobal);
    }

    public int skipThread(long msg) throws UnexpectedException, ObjectNotFoundException, NoCurrentMessageException, SelectionOverflowException
    {
        return underlying.skipThread(msg);
    }

    public FileStatus statFile(long parent, String name) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.statFile(parent, name);
    }

    public void storeFile(long parent, String name, String content, int permissions) throws AuthorizationException, ObjectNotFoundException, UnexpectedException
    {
        underlying.storeFile(parent, name, content, permissions);
    }

    public MessageOccurrence storeMail(long recipient, UnstoredMessage msg) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.storeMail(recipient, msg);
    }

    public MessageOccurrence storeMessage(long conf, UnstoredMessage msg) throws ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        return underlying.storeMessage(conf, msg);
    }

    public void storeNoComment(MessageLocator message) throws AuthorizationException, NoCurrentMessageException, ObjectNotFoundException, UnexpectedException
    {
        underlying.storeNoComment(message);
    }

    public MessageOccurrence storePresentation(UnstoredMessage msg, long object) throws UnexpectedException, AuthorizationException, ObjectNotFoundException
    {
        return underlying.storePresentation(msg, object);
    }

    public MessageOccurrence storeRulePosting(UnstoredMessage msg) throws AuthorizationException, UnexpectedException, ObjectNotFoundException
    {
        return underlying.storeRulePosting(msg);
    }

    public void storeSystemFile(String name, String content) throws AuthorizationException, UnexpectedException
    {
        underlying.storeSystemFile(name, content);
    }

    public void updateCharacterset(String charset) throws UnexpectedException
    {
        underlying.updateCharacterset(charset);
    }

    public void updateConferencePermissions(long id, int permissions, int nonmemberpermissions, short visibility) throws ObjectNotFoundException, AuthorizationException, UnexpectedException
    {
        underlying.updateConferencePermissions(id, permissions, nonmemberpermissions, visibility);
    }

    public void updateLastlogin() throws ObjectNotFoundException, UnexpectedException
    {
        underlying.updateLastlogin();
    }

    public void updateTimeZone(String timeZone) throws UnexpectedException
    {
        underlying.updateTimeZone(timeZone);
    }

    public boolean userCanChangeNameOf(long id) throws DuplicateNameException, UnexpectedException
    {
        return underlying.userCanChangeNameOf(id);
    }

    public int[] verifyChatRecipients(long[] recepipents) throws ObjectNotFoundException, UnexpectedException
    {
        return underlying.verifyChatRecipients(recepipents);
    }

    public NameAssociation[] findObjects(String pattern) throws UnexpectedException
    {
        return underlying.findObjects(pattern);
    }

    public void changeKeywords(long id, String keywords) throws UnexpectedException, ObjectNotFoundException, AuthorizationException
    {
        underlying.changeKeywords(id, keywords);
    }

    public void changeEmailAlias(long id, String emailAlias) throws UnexpectedException, ObjectNotFoundException, AuthorizationException
    {
        underlying.changeEmailAlias(id, emailAlias);
    }    
    
    public Envelope readMessage(MessageLocator message) throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        return underlying.readMessage(message);
    }

    public void addBookmark(MessageLocator message, String annotation) throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        underlying.addBookmark(message, annotation);
    }

    public void deleteBookmark(MessageLocator message) throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        underlying.deleteBookmark(message);
    }

    public Bookmark[] listBookmarks() throws UnexpectedException
    {
        return underlying.listBookmarks();
    }

    public MessageOccurrence storeReplyAsMessage(long conference, UnstoredMessage msg, MessageLocator replyTo) throws ObjectNotFoundException, UnexpectedException, AuthorizationException, NoCurrentMessageException
    {
        return underlying.storeReplyAsMessage(conference, msg, replyTo);
    }

    public MessageOccurrence storeReplyAsMail(long recipient, UnstoredMessage msg, MessageLocator replyTo) throws ObjectNotFoundException, UnexpectedException, NoCurrentMessageException
    {
        return underlying.storeReplyAsMail(recipient, msg, replyTo);
    }

    public MessageLocator resolveLocator(MessageLocator message) throws ObjectNotFoundException, NoCurrentMessageException, UnexpectedException
    {
        return underlying.resolveLocator(message);
    }
}
