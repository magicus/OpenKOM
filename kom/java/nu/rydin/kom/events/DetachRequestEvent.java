package nu.rydin.kom.events;

public class DetachRequestEvent extends SingleUserEvent
{
    public DetachRequestEvent(long originatingUser, long targetUser)
    {
        super(originatingUser, targetUser);
    }
}
