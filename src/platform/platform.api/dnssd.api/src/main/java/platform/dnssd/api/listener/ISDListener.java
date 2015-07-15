/**
 * @file ISDListener.java
 * @brief <description>
 */

package platform.dnssd.api.listener;

import java.util.List;

import platform.dnssd.api.filter.SDEntityBrowseResult;

public interface ISDListener
{
    void serviceResolved(List<SDEntityBrowseResult> serviceBrowseResultList);
}
