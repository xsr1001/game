/**
 * @file ISDListener.java
 * @brief Service discovery listener. Provides a callback to being notified from a SD manager when services are resolved.
 */

package platform.dnssd.api.listener;

import java.util.List;

import platform.dnssd.api.filter.ServiceBrowseResult;

/**
 * Service discovery browse listener. Provides a callback to being notified from a SD manager when services are
 * resolved.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface ISDListener
{

    /**
     * Callback for resolved service data notification.
     * 
     * @param serviceBrowseResultList
     *            - a {@link List} of {@link ServiceBrowseResult} objects containing resolved service data.
     */
    void serviceResolved(List<ServiceBrowseResult> serviceBrowseResultList);
}
