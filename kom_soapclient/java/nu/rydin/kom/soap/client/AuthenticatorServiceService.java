/**
 * AuthenticatorServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package nu.rydin.kom.soap.client;

public interface AuthenticatorServiceService extends javax.xml.rpc.Service {
    public java.lang.String getAuthenticatorAddress();

    public nu.rydin.kom.soap.client.AuthenticatorService getAuthenticator() throws javax.xml.rpc.ServiceException;

    public nu.rydin.kom.soap.client.AuthenticatorService getAuthenticator(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
