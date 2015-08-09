/**
 * @file ISDBrowseResultListener.java
 * @brief Internal interface for notify service browse results to the upstream service discovery manager.
 */

package platform.dnssd.listener;

import platform.dnssd.api.filter.ServiceBrowseResult;

/**
 * Internal interface for notify service browse results to the upstream service discovery manager.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface ISDBrowseResultListener
{
    /**
     * Notify listener with resolved service data.
     * 
     * @param browseResult
     *            - a {@link ServiceBrowseResult} object containing resolved service data.
     */
    void notifyServiceResolved(ServiceBrowseResult browseResult);

    /**
     * Notify listener with remove service data.
     * 
     * @param browseResult
     *            - a {@link ServiceBrowseResult} object containing removed service data.
     */
    void notifyServiceRemoved(ServiceBrowseResult browseResult);
}
