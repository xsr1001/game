/**
 * @file SDServiceEntry.java
 * @brief Service browsed result object containing resolved service data.
 */

package platform.dnssd.api.filter;

import java.net.Inet4Address;
import java.util.Map;

/**
 * Service browsed result object containing resolved service data.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class ServiceBrowseResult
{
    // A short service type. Equivalent to the service type provided to browse call.
    private String serviceType;

    // A long service type. Equivalent to the fully qualified jmDNS service type in format:
    // [_<serviceType>._<protocol>.<subDomain>.<domain>.]
    private String serviceFullType;

    // Resolved service name.
    private String serviceName;

    // Service context. Contains arbitrary amount of key-value pairs that have been supplied at advertise call.
    private Map<String, String> serviceContext;

    // An array of resolved service IPv4 addresses and ports.
    private Inet4Address[] inet4AddressArray;

    /**
     * Constructor.
     * 
     * @param serviceFullType
     *            - a {@link String} long format service type. Equivalent to the fully qualified jmDNS service type in
     *            format: [_<serviceType>._<protocol>.<subDomain>.<domain>.]
     * @param serviceType
     *            - a short format service type. Equal to service application protocol and equal to the service type,
     *            supplied at advertise call.
     * @param serviceName
     *            - a {@link String} service name.
     * @param serviceContext
     *            - a {@link Map} of {@link String} key to {@link String} value pairs. Contains an arbitrary amount of
     *            keys supplied at advertise call.
     * @param inet4AddressArray
     *            - an array of {@link Inet4Address} resolved service addresses.
     */
    public ServiceBrowseResult(String serviceFullType, String serviceType, String serviceName,
        Map<String, String> serviceContext, Inet4Address[] inet4AddressArray)
    {
        this.serviceType = serviceType;
        this.serviceFullType = serviceFullType;
        this.serviceName = serviceName;
        this.serviceContext = serviceContext;
        this.inet4AddressArray = inet4AddressArray;
    }

    public String getServiceType()
    {
        return serviceType;
    }

    public String getServiceFullType()
    {
        return serviceFullType;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public Map<String, String> getServiceContext()
    {
        return serviceContext;
    }

    public Inet4Address[] getInet4AddressArray()
    {
        return inet4AddressArray;
    }
}
