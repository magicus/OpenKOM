/**
 * MessagePosterService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package nu.rydin.kom.soap.client;

public interface MessagePosterService extends java.rmi.Remote {
    public nu.rydin.kom.soap.client.MessageOccurrence storeMessage(nu.rydin.kom.soap.client.SecurityToken token, long conf, nu.rydin.kom.soap.client.UnstoredMessage msg) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.ObjectNotFoundException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.SessionExpiredException, nu.rydin.kom.soap.client.AuthorizationException;
    public nu.rydin.kom.soap.client.MessageOccurrence storeMail(nu.rydin.kom.soap.client.SecurityToken token, long recipient, nu.rydin.kom.soap.client.UnstoredMessage msg) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.ObjectNotFoundException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.SessionExpiredException;
    public nu.rydin.kom.soap.client.MessageOccurrence storeReplyAsMessage(nu.rydin.kom.soap.client.SecurityToken token, long conference, nu.rydin.kom.soap.client.UnstoredMessage msg, long replyTo) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.ObjectNotFoundException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.SessionExpiredException, nu.rydin.kom.soap.client.AuthorizationException;
    public nu.rydin.kom.soap.client.MessageOccurrence storeReplyAsMail(nu.rydin.kom.soap.client.SecurityToken token, long recipient, nu.rydin.kom.soap.client.UnstoredMessage msg, long replyTo) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.ObjectNotFoundException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.SessionExpiredException;
}
