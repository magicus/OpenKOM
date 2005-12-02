/**
 * AuthenticatorServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package nu.rydin.kom.soap.client;

public class AuthenticatorServiceServiceLocator extends org.apache.axis.client.Service implements nu.rydin.kom.soap.client.AuthenticatorServiceService {

    public AuthenticatorServiceServiceLocator() {
    }


    public AuthenticatorServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AuthenticatorServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Authenticator
    private java.lang.String Authenticator_address = "http://localhost:8080/axis/services/Authenticator";

    public java.lang.String getAuthenticatorAddress() {
        return Authenticator_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AuthenticatorWSDDServiceName = "Authenticator";

    public java.lang.String getAuthenticatorWSDDServiceName() {
        return AuthenticatorWSDDServiceName;
    }

    public void setAuthenticatorWSDDServiceName(java.lang.String name) {
        AuthenticatorWSDDServiceName = name;
    }

    public nu.rydin.kom.soap.client.AuthenticatorService getAuthenticator() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Authenticator_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAuthenticator(endpoint);
    }

    public nu.rydin.kom.soap.client.AuthenticatorService getAuthenticator(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            nu.rydin.kom.soap.client.AuthenticatorSoapBindingStub _stub = new nu.rydin.kom.soap.client.AuthenticatorSoapBindingStub(portAddress, this);
            _stub.setPortName(getAuthenticatorWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAuthenticatorEndpointAddress(java.lang.String address) {
        Authenticator_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (nu.rydin.kom.soap.client.AuthenticatorService.class.isAssignableFrom(serviceEndpointInterface)) {
                nu.rydin.kom.soap.client.AuthenticatorSoapBindingStub _stub = new nu.rydin.kom.soap.client.AuthenticatorSoapBindingStub(new java.net.URL(Authenticator_address), this);
                _stub.setPortName(getAuthenticatorWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("Authenticator".equals(inputPortName)) {
            return getAuthenticator();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "AuthenticatorServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "Authenticator"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Authenticator".equals(portName)) {
            setAuthenticatorEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
