//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.01 at 01:18:33 PM CEST 
//


package platform.service_two.configuration.schema;

import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the platform.service_two.configuration.schema package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: platform.service_two.configuration.schema
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TAdminServiceConfiguration100 }
     * 
     */
    public TAdminServiceConfiguration100 createTAdminServiceConfiguration100() {
        return new TAdminServiceConfiguration100();
    }

    /**
     * Create an instance of {@link TAdminServiceConfigurationBase100 }
     * 
     */
    public TAdminServiceConfigurationBase100 createTAdminServiceConfigurationBase100() {
        return new TAdminServiceConfigurationBase100();
    }

    /**
     * Create an instance of {@link TAdminServiceConfiguration100 .AdminServiceSpecificElement }
     * 
     */
    public TAdminServiceConfiguration100 .AdminServiceSpecificElement createTAdminServiceConfiguration100AdminServiceSpecificElement() {
        return new TAdminServiceConfiguration100 .AdminServiceSpecificElement();
    }

    /**
     * Create an instance of {@link AdminServiceConfiguration100 }}
     * 
     */
    @XmlElementDecl(namespace = "http://admin/service/configuration/v2.0.0/", name = "adminServiceConfiguration", substitutionHeadNamespace = "http://platform/service/configuration/", substitutionHeadName = "configuration")
    public AdminServiceConfiguration100 createAdminServiceConfiguration100(TAdminServiceConfiguration100 value) {
        return new AdminServiceConfiguration100(value);
    }

}