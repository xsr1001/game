//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.01 at 12:10:17 PM CEST 
//


package platform.service.configuration.schema;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class ServiceConfiguration
    extends JAXBElement<TServiceConfiguration>
{

    protected final static QName NAME = new QName("http://platform/service/configuration/", "serviceConfiguration");

    public ServiceConfiguration(TServiceConfiguration value) {
        super(NAME, ((Class) TServiceConfiguration.class), null, value);
    }

    public ServiceConfiguration() {
        super(NAME, ((Class) TServiceConfiguration.class), null, null);
    }

}
