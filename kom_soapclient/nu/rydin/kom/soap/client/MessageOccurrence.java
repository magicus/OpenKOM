/**
 * MessageOccurrence.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package nu.rydin.kom.soap.client;

public class MessageOccurrence  implements java.io.Serializable {
    private long globalId;
    private short kind;
    private int localnum;
    private java.util.Calendar timestamp;
    private nu.rydin.kom.soap.client.NameAssociation user;

    public MessageOccurrence() {
    }

    public MessageOccurrence(
           long globalId,
           short kind,
           int localnum,
           java.util.Calendar timestamp,
           nu.rydin.kom.soap.client.NameAssociation user) {
           this.globalId = globalId;
           this.kind = kind;
           this.localnum = localnum;
           this.timestamp = timestamp;
           this.user = user;
    }


    /**
     * Gets the globalId value for this MessageOccurrence.
     * 
     * @return globalId
     */
    public long getGlobalId() {
        return globalId;
    }


    /**
     * Sets the globalId value for this MessageOccurrence.
     * 
     * @param globalId
     */
    public void setGlobalId(long globalId) {
        this.globalId = globalId;
    }


    /**
     * Gets the kind value for this MessageOccurrence.
     * 
     * @return kind
     */
    public short getKind() {
        return kind;
    }


    /**
     * Sets the kind value for this MessageOccurrence.
     * 
     * @param kind
     */
    public void setKind(short kind) {
        this.kind = kind;
    }


    /**
     * Gets the localnum value for this MessageOccurrence.
     * 
     * @return localnum
     */
    public int getLocalnum() {
        return localnum;
    }


    /**
     * Sets the localnum value for this MessageOccurrence.
     * 
     * @param localnum
     */
    public void setLocalnum(int localnum) {
        this.localnum = localnum;
    }


    /**
     * Gets the timestamp value for this MessageOccurrence.
     * 
     * @return timestamp
     */
    public java.util.Calendar getTimestamp() {
        return timestamp;
    }


    /**
     * Sets the timestamp value for this MessageOccurrence.
     * 
     * @param timestamp
     */
    public void setTimestamp(java.util.Calendar timestamp) {
        this.timestamp = timestamp;
    }


    /**
     * Gets the user value for this MessageOccurrence.
     * 
     * @return user
     */
    public nu.rydin.kom.soap.client.NameAssociation getUser() {
        return user;
    }


    /**
     * Sets the user value for this MessageOccurrence.
     * 
     * @param user
     */
    public void setUser(nu.rydin.kom.soap.client.NameAssociation user) {
        this.user = user;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof MessageOccurrence)) return false;
        MessageOccurrence other = (MessageOccurrence) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.globalId == other.getGlobalId() &&
            this.kind == other.getKind() &&
            this.localnum == other.getLocalnum() &&
            ((this.timestamp==null && other.getTimestamp()==null) || 
             (this.timestamp!=null &&
              this.timestamp.equals(other.getTimestamp()))) &&
            ((this.user==null && other.getUser()==null) || 
             (this.user!=null &&
              this.user.equals(other.getUser())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += new Long(getGlobalId()).hashCode();
        _hashCode += getKind();
        _hashCode += getLocalnum();
        if (getTimestamp() != null) {
            _hashCode += getTimestamp().hashCode();
        }
        if (getUser() != null) {
            _hashCode += getUser().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(MessageOccurrence.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:MessagePoster", "MessageOccurrence"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("globalId");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:MessagePoster", "globalId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("kind");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:MessagePoster", "kind"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "short"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("localnum");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:MessagePoster", "localnum"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timestamp");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:MessagePoster", "timestamp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("user");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:MessagePoster", "user"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:MessagePoster", "NameAssociation"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
