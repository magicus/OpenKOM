/**
 * MessagePosterServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package nu.rydin.kom.soap.client;

public class MessagePosterServiceServiceLocator extends org.apache.axis.client.Service implements nu.rydin.kom.soap.client.MessagePosterServiceService {

    public MessagePosterServiceServiceLocator() {
    }


    public MessagePosterServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MessagePosterServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for MessagePoster
    private java.lang.String MessagePoster_address = "http://localhost:8080/axis/services/MessagePoster";

    public java.lang.String getMessagePosterAddress() {
        return MessagePoster_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MessagePosterWSDDServiceName = "MessagePoster";

    public java.lang.String getMessagePosterWSDDServiceName() {
        return MessagePosterWSDDServiceName;
    }

    public void setMessagePosterWSDDServiceName(java.lang.String name) {
        MessagePosterWSDDServiceName = name;
    }

    public nu.rydin.kom.soap.client.MessagePosterService getMessagePoster() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MessagePoster_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMessagePoster(endpoint);
    }

    public nu.rydin.kom.soap.client.MessagePosterService getMessagePoster(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            nu.rydin.kom.soap.client.MessagePosterSoapBindingStub _stub = new nu.rydin.kom.soap.client.MessagePosterSoapBindingStub(portAddress, this);
            _stub.setPortName(getMessagePosterWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMessagePosterEndpointAddress(java.lang.String address) {
        MessagePoster_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (nu.rydin.kom.soap.client.MessagePosterService.class.isAssignableFrom(serviceEndpointInterface)) {
                nu.rydin.kom.soap.client.MessagePosterSoapBindingStub _stub = new nu.rydin.kom.soap.client.MessagePosterSoapBindingStub(new java.net.URL(MessagePoster_address), this);
                _stub.setPortName(getMessagePosterWSDDServiceName());
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
        if ("MessagePoster".equals(inputPortName)) {
            return getMessagePoster();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://localhost:8080/axis/services/MessagePoster", "MessagePosterServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://localhost:8080/axis/services/MessagePoster", "MessagePoster"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("MessagePoster".equals(portName)) {
            setMessagePosterEndpointAddress(address);
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
