/**
 * @file IServerListener.java
 * @brief IServerListener for receiving channel life-cycle notifications.
 */

package game.usn.bridge.api.listener;

/**
 * IChannelListener interface provides callback functionality for receiving channel life-cycle notifications. Client
 * connect/disconnect and service bind/unbind results are propagated using these callbacks.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IChannelListener
{
    /**
     * Notify channel up. This represents successful bind for service channel and successful connect with remote USN
     * end-point.
     * 
     * @param proxyName
     *            - a {@link String} proxy name that registered itself to this channel
     */
    public void notifyChannelUp(String proxyName);

    /**
     * Notify channel down. This represents successful unbind for service channel and successful disconnect with remote
     * USN end-point.
     * 
     * @param proxyName
     *            - a {@link String} proxy name that unregistered itself to this channel
     */
    public void notifyChannelDown(String proxyName);

    /**
     * Notify channel error. This represents error during proxy registration or connect/bind operation.
     * 
     * @param proxyName
     *            - a {@link String} proxy name that registered/unregistered itself to this channel
     */
    public void notifyChannelError(String proxyName);
}
