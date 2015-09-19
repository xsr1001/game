/**
 * @file IChannelObserver.java
 * @brief IChannelObserver for receiving channel life-cycle change notifications.
 */

package platform.bridge.api.observer;

import java.net.InetSocketAddress;

/**
 * IChannelObserver interface provides callback functionality for receiving channel life-cycle change notifications.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IChannelObserver
{
    /**
     * Notify observers with a channel state change.
     * 
     * @param isChannelUp
     *            - a flag determining if a channel is up or down. Concrete proxy type (service/client) determines the
     *            context of the flag.
     * @param proxyName
     *            - a {@link String} name of the proxy channel life-cycle has been changed on.
     * @param inetSocketAddress
     *            - a {@link InetSocketAddress} associated with this channel life-cycle change. Local service address is
     *            returned on service channel life-cycle change or a remote service channel on client channel life-cycle
     *            change.
     */
    void notifyChannelStateChanged(boolean isChannelUp, String proxyName, InetSocketAddress inetSocketAddress);
}
