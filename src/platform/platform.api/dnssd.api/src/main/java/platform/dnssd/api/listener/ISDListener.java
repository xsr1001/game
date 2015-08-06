/**
 * @file ISDListener.java
 * @brief <description>
 */

package platform.dnssd.api.listener;

import java.util.List;

import platform.dnssd.api.filter.ServiceBrowseResult;

public interface ISDListener
{
    void serviceResolved(List<ServiceBrowseResult> serviceBrowseResultList);
}
