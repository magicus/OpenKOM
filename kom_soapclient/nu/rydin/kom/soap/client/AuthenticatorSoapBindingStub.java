/**
 * AuthenticatorSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package nu.rydin.kom.soap.client;

public class AuthenticatorSoapBindingStub extends org.apache.axis.client.Stub implements nu.rydin.kom.soap.client.AuthenticatorService {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[2];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("login");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.soap.kom.rydin.nu", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.soap.kom.rydin.nu", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:Authenticator", "SecurityToken"));
        oper.setReturnClass(nu.rydin.kom.soap.client.SecurityToken.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.soap.kom.rydin.nu", "loginReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "fault3"),
                      "nu.rydin.kom.soap.client.messageposter.LoginProhibitedException",
                      new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "LoginProhibitedException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "fault1"),
                      "nu.rydin.kom.soap.client.messageposter.UnexpectedException",
                      new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "UnexpectedException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "fault"),
                      "nu.rydin.kom.soap.client.messageposter.AuthenticationException",
                      new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "AuthenticationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "fault2"),
                      "nu.rydin.kom.soap.client.messageposter.AlreadyLoggedInException",
                      new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "AlreadyLoggedInException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("discardToken");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.soap.kom.rydin.nu", "token"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:Authenticator", "SecurityToken"), nu.rydin.kom.soap.client.SecurityToken.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "fault1"),
                      "nu.rydin.kom.soap.client.messageposter.UnexpectedException",
                      new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "UnexpectedException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "fault"),
                      "nu.rydin.kom.soap.client.messageposter.AuthenticationException",
                      new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "AuthenticationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://localhost:8080/axis/services/Authenticator", "fault4"),
                      "nu.rydin.kom.soap.client.messageposter.SessionExpiredException",
                      new javax.xml.namespace.QName("http://exceptions.soap.kom.rydin.nu", "SessionExpiredException"), 
                      true
                     ));
        _operations[1] = oper;

    }

    public AuthenticatorSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public AuthenticatorSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public AuthenticatorSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "AlreadyLoggedInException");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.AlreadyLoggedInException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "AuthenticationException");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.AuthenticationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "KOMException");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.KOMException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "LoginProhibitedException");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.LoginProhibitedException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "SystemException");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.SystemException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.kom.rydin.nu", "UnexpectedException");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.UnexpectedException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exceptions.soap.kom.rydin.nu", "SessionExpiredException");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.SessionExpiredException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:Authenticator", "SecurityToken");
            cachedSerQNames.add(qName);
            cls = nu.rydin.kom.soap.client.SecurityToken.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public nu.rydin.kom.soap.client.SecurityToken login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.LoginProhibitedException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.AuthenticationException, nu.rydin.kom.soap.client.AlreadyLoggedInException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.soap.kom.rydin.nu", "login"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username, password});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (nu.rydin.kom.soap.client.SecurityToken) _resp;
            } catch (java.lang.Exception _exception) {
                return (nu.rydin.kom.soap.client.SecurityToken) org.apache.axis.utils.JavaUtils.convert(_resp, nu.rydin.kom.soap.client.SecurityToken.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof nu.rydin.kom.soap.client.LoginProhibitedException) {
              throw (nu.rydin.kom.soap.client.LoginProhibitedException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof nu.rydin.kom.soap.client.UnexpectedException) {
              throw (nu.rydin.kom.soap.client.UnexpectedException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof nu.rydin.kom.soap.client.AuthenticationException) {
              throw (nu.rydin.kom.soap.client.AuthenticationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof nu.rydin.kom.soap.client.AlreadyLoggedInException) {
              throw (nu.rydin.kom.soap.client.AlreadyLoggedInException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void discardToken(nu.rydin.kom.soap.client.SecurityToken token) throws java.rmi.RemoteException, nu.rydin.kom.soap.client.UnexpectedException, nu.rydin.kom.soap.client.AuthenticationException, nu.rydin.kom.soap.client.SessionExpiredException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.soap.kom.rydin.nu", "discardToken"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {token});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof nu.rydin.kom.soap.client.UnexpectedException) {
              throw (nu.rydin.kom.soap.client.UnexpectedException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof nu.rydin.kom.soap.client.AuthenticationException) {
              throw (nu.rydin.kom.soap.client.AuthenticationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof nu.rydin.kom.soap.client.SessionExpiredException) {
              throw (nu.rydin.kom.soap.client.SessionExpiredException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
