//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.08.01 at 12:10:17 PM CEST 
//

package platform.service.configuration.schema;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for t_service_configuration complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="t_service_configuration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="platform">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="SDContext">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="contextManagerClassBinding" use="required">
 *                             &lt;simpleType>
 *                               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                                 &lt;enumeration value="platform.sd.context.manager.impl.DevSDContextManager"/>
 *                                 &lt;enumeration value="platform.sd.context.manager.impl.TestSDContextManager"/>
 *                                 &lt;enumeration value="platform.sd.context.manager.impl.ProdSDContextManager"/>
 *                                 &lt;enumeration value="platform.sd.context.manager.impl.BetaSDContextManager"/>
 *                               &lt;/restriction>
 *                             &lt;/simpleType>
 *                           &lt;/attribute>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="domain">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="service">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://platform/service/configuration/}configuration"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_service_configuration", propOrder = { "platform", "service" })
public class TServiceConfiguration
{

    @XmlElement(required = true)
    protected TServiceConfiguration.Platform platform;
    @XmlElement(required = true)
    protected TServiceConfiguration.Service service;

    /**
     * Gets the value of the platform property.
     * 
     * @return possible object is {@link TServiceConfiguration.Platform }
     * 
     */
    public TServiceConfiguration.Platform getPlatform()
    {
        return platform;
    }

    /**
     * Sets the value of the platform property.
     * 
     * @param value
     *            allowed object is {@link TServiceConfiguration.Platform }
     * 
     */
    public void setPlatform(TServiceConfiguration.Platform value)
    {
        this.platform = value;
    }

    /**
     * Gets the value of the service property.
     * 
     * @return possible object is {@link TServiceConfiguration.Service }
     * 
     */
    public TServiceConfiguration.Service getService()
    {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value
     *            allowed object is {@link TServiceConfiguration.Service }
     * 
     */
    public void setService(TServiceConfiguration.Service value)
    {
        this.service = value;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="SDContext">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="contextManagerClassBinding" use="required">
     *                   &lt;simpleType>
     *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *                       &lt;enumeration value="platform.sd.context.manager.impl.DevSDContextManager"/>
     *                       &lt;enumeration value="platform.sd.context.manager.impl.TestSDContextManager"/>
     *                       &lt;enumeration value="platform.sd.context.manager.impl.ProdSDContextManager"/>
     *                       &lt;enumeration value="platform.sd.context.manager.impl.BetaSDContextManager"/>
     *                     &lt;/restriction>
     *                   &lt;/simpleType>
     *                 &lt;/attribute>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="domain">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "sdContext", "domain" })
    public static class Platform
    {

        @XmlElement(name = "SDContext", required = true)
        protected TServiceConfiguration.Platform.SDContext sdContext;
        @XmlElement(required = true)
        protected TServiceConfiguration.Platform.Domain domain;

        /**
         * Gets the value of the sdContext property.
         * 
         * @return possible object is {@link TServiceConfiguration.Platform.SDContext }
         * 
         */
        public TServiceConfiguration.Platform.SDContext getSDContext()
        {
            return sdContext;
        }

        /**
         * Sets the value of the sdContext property.
         * 
         * @param value
         *            allowed object is {@link TServiceConfiguration.Platform.SDContext }
         * 
         */
        public void setSDContext(TServiceConfiguration.Platform.SDContext value)
        {
            this.sdContext = value;
        }

        /**
         * Gets the value of the domain property.
         * 
         * @return possible object is {@link TServiceConfiguration.Platform.Domain }
         * 
         */
        public TServiceConfiguration.Platform.Domain getDomain()
        {
            return domain;
        }

        /**
         * Sets the value of the domain property.
         * 
         * @param value
         *            allowed object is {@link TServiceConfiguration.Platform.Domain }
         * 
         */
        public void setDomain(TServiceConfiguration.Platform.Domain value)
        {
            this.domain = value;
        }

        /**
         * <p>
         * Java class for anonymous complex type.
         * 
         * <p>
         * The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Domain
        {

            @XmlAttribute(name = "value", required = true)
            protected String value;

            /**
             * Gets the value of the value property.
             * 
             * @return possible object is {@link String }
             * 
             */
            public String getValue()
            {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *            allowed object is {@link String }
             * 
             */
            public void setValue(String value)
            {
                this.value = value;
            }

        }

        /**
         * <p>
         * Java class for anonymous complex type.
         * 
         * <p>
         * The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="contextManagerClassBinding" use="required">
         *         &lt;simpleType>
         *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
         *             &lt;enumeration value="platform.sd.context.manager.impl.DevSDContextManager"/>
         *             &lt;enumeration value="platform.sd.context.manager.impl.TestSDContextManager"/>
         *             &lt;enumeration value="platform.sd.context.manager.impl.ProdSDContextManager"/>
         *             &lt;enumeration value="platform.sd.context.manager.impl.BetaSDContextManager"/>
         *           &lt;/restriction>
         *         &lt;/simpleType>
         *       &lt;/attribute>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SDContext
        {

            @XmlAttribute(name = "contextManagerClassBinding", required = true)
            protected String contextManagerClassBinding;

            /**
             * Gets the value of the contextManagerClassBinding property.
             * 
             * @return possible object is {@link String }
             * 
             */
            public String getContextManagerClassBinding()
            {
                return contextManagerClassBinding;
            }

            /**
             * Sets the value of the contextManagerClassBinding property.
             * 
             * @param value
             *            allowed object is {@link String }
             * 
             */
            public void setContextManagerClassBinding(String value)
            {
                this.contextManagerClassBinding = value;
            }

        }

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{http://platform/service/configuration/}configuration"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "configuration" })
    public static class Service
    {

        @XmlElementRef(name = "configuration", namespace = "http://platform/service/configuration/", type = Configuration.class)
        protected JAXBElement<? extends TConfigurationBase> configuration;

        /**
         * Gets the value of the configuration property.
         * 
         * @return possible object is {@link Configuration }
         * 
         */
        public JAXBElement<? extends TConfigurationBase> getConfiguration()
        {
            return configuration;
        }

        /**
         * Sets the value of the configuration property.
         * 
         * @param value
         *            allowed object is {@link Configuration }
         * 
         */
        public void setConfiguration(JAXBElement<? extends TConfigurationBase> value)
        {
            this.configuration = value;
        }

    }

}
