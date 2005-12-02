/**
 * AuthenticatorService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package nu.rydin.kom.soap.client;

public interface AuthenticatorService extends java.rmi.Remote {
    public nu.rydin.kom.soap.client.SecurityToken login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.LoginProhibitedException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.AuthenticationException, nu.rydin.kom.soap.client.AlreadyLoggedInException;
    public void discardToken(nu.rydin.kom.soap.client.SecurityToken token) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.AuthenticationException, nu.rydin.kom.soap.client.SessionExpiredException;
}
