//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.17 at 06:57:47 PM CEST 
//


package platform.service.configuration.schema;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class Configuration
    extends JAXBElement<TConfigurationBase>
{

    protected final static QName NAME = new QName("http://platform/service/configuration/", "configuration");

    public Configuration(TConfigurationBase value) {
        super(NAME, ((Class) TConfigurationBase.class), null, value);
    }

    public Configuration() {
        super(NAME, ((Class) TConfigurationBase.class), null, null);
    }

}
