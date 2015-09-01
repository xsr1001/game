/**
 * @file IChannelObserver.java
 * @brief IChannelObserver for receiving channel life-cycle notifications.
 */

package platform.bridge.api.listener;

import java.net.InetSocketAddress;

/**
 * IChannelObserver interface provides callback functionality for receiving channel life-cycle notifications. Client
 * connect/disconnect and service bind/unbind results are propagated using these callbacks.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IChannelObserver
{
    /**
     * Notify channel up. This represents successful bind for service channel or successful connect with remote service
     * 
     * @param proxyName
     *            - a {@link String} proxy name that registered itself to this channel.
     * @param address
     *            - a {@link InetSocketAddress} address on which bridge operation executed successfully. Can represent
     *            local address for bind operation or remote address for connect operation.
     */
    void notifyChannelUp(String proxyName, InetSocketAddress address);

    /**
     * Notify channel down. This represents successful unbind for service channel and successful disconnect with remote
     * service.
     * 
     * @param proxyName
     *            - a {@link String} proxy name that unregistered itself to this channel.
     */
    void notifyChannelDown(String proxyName);
}
