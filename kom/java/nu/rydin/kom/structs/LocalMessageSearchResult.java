/*
 * Created on Sep 12, 2004
 * 
 * Distributed under the GPL license.
 * See http://www.gnu.org for details
 */
package nu.rydin.kom.structs;

/**
 * @author Henrik
 */
public class LocalMessageSearchResult extends MessageSearchResult
{
    public LocalMessageSearchResult(long globalid, int localid, NameAssociation author, String subject, long replyTo)
    {
        super(globalid, localid, author, subject, replyTo);
    }
}
