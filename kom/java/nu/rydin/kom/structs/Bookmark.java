/*
 * Oct 25, 2006
 *
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author Pontus Rydin
 */
public class Bookmark
{
    private final long user;
    private final long message;
    private final String annotation;
    
    public Bookmark(long user, long message, String annotation)
    {
        this.user = user;
        this.message = message;
        this.annotation = annotation;
    }

    public String getAnnotation()
    {
        return annotation;
    }

    public long getMessage()
    {
        return message;
    }

    public long getUser()
    {
        return user;
    }
}
