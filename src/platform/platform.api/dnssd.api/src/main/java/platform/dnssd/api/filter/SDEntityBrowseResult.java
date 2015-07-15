/**
 * @file SDServiceEntry.java
 * @brief <description>
 */

package platform.dnssd.api.filter;

import java.net.Inet4Address;
import java.util.Map;

public class SDEntityBrowseResult
{
    private String type;
    private String name;
    private Map<String, String> sdEntityContext;
    private Inet4Address[] inet4AddressArray;

    public SDEntityBrowseResult(String type, String name, Map<String, String> sdEntityContext,
        Inet4Address[] inet4AddressArray)
    {

    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getSdEntityContext()
    {
        return sdEntityContext;
    }

    public void setSdEntityContext(Map<String, String> sdEntityContext)
    {
        this.sdEntityContext = sdEntityContext;
    }

    public Inet4Address[] getInet4AddressArray()
    {
        return inet4AddressArray;
    }

    public void setInet4AddressArray(Inet4Address[] inet4AddressArray)
    {
        this.inet4AddressArray = inet4AddressArray;
    }

}
