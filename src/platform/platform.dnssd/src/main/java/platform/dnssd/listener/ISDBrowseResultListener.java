/**
 * @file ISDBrowseResultListener.java
 * @brief <description>
 */

package platform.dnssd.listener;

import platform.dnssd.api.filter.ServiceBrowseResult;

public interface ISDBrowseResultListener
{
    void notifyServiceResolved(ServiceBrowseResult browseResult);

    void notifyServiceRemoved(ServiceBrowseResult browseResult);
}
