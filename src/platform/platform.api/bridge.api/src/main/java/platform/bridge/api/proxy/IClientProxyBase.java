/**
 * @file IClientProxy.java
 * @brief Client proxy base defines network base implementation independent client proxy capabilities.
 */

package platform.bridge.api.proxy;

import platform.bridge.api.protocol.AbstractPacket;
import platform.core.api.exception.BridgeException;

/**
 * Client proxy base defines network base implementation independent client proxy capabilities.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IClientProxyBase
{
    /**
     * Attempt to send a packet via network base implementation to the remote service.
     * 
     * @param packet
     *            - a {@link AbstractPacket} to send.
     * @throws BridgeException
     *             - throws {@link BridgeException} on send error.
     */
    void sendPacket(AbstractPacket packet) throws BridgeException;

    /**
     * Release a client proxy and cleanup.
     * 
     * @throws BridgeException
     *             - throws {@link BridgeException} on proxy release error.
     */
    void release() throws BridgeException;

    /**
     * Initialize a client proxy.
     * 
     * @param serviceIPv4Address
     *            - a {@link String} IPv4 address of a remote service to connect with.
     * @param servicePort
     *            - a {@link Integer} service port of a remote service to connect on.
     * @param responseListener
     *            - a {@link IResponseListener} listener for remote service responses. Provides a chain to retrieve
     *            specific proxy implementation connection settings.
     * @throws BridgeException
     *             - throws {@link BridgeException} on proxy initialization error.
     */
    void initialize(String serviceIPv4Address, Integer servicePort, IResponseListener responseListener)
        throws BridgeException;
}
