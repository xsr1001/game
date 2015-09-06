/**
 * @file IResponseListener.java
 * @brief Response listener defines capabilities for a proxy to receive data from network base.
 */

package platform.bridge.api.proxy;

import java.util.List;
import java.util.Set;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;

/**
 * Response listener defines capabilities for a proxy to receive data from network base. It defines additional network
 * base specific requirements for a proxy.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IResponseListener extends IChannelObserver
{
    /**
     * Receive a packet from network proxy base.
     * 
     * @param abstractPacket
     *            - a {@link AbstractPacket} to receive.
     * @param senderIdentifier
     *            - a {@link String} sender identifier.
     */
    void receive(AbstractPacket abstractPacket, String senderIdentifier);

    /**
     * Retrieve proxy specific bridge options.
     * 
     * @return - a {@link BridgeOptions} object, defining basic options to initialize network channel with.
     */
    BridgeOptions getBridgeOptions();

    /**
     * Retrieve proxy implementation specific name.
     * 
     * @return - a {@link String} proxy name.
     */
    String getName();

    /**
     * Retrieve the proxy specific platform protocol.
     * 
     * @return - an instance of {@link AbstractPlatformProtocol}.
     */
    AbstractPlatformProtocol getProtocol();

    /**
     * Retrieve proxy specific channel observer set.
     * 
     * @return - a {@link List} of {@link IChannelObserver} objects, representing observers to network channel
     *         life-cycle events.
     */
    Set<IChannelObserver> getChannelObserverSet();
}
