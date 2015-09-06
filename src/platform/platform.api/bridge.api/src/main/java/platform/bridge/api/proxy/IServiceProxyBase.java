/**
 * @file IServiceProxyBase.java
 * @brief Service proxy base defines network base implementation independent service proxy capabilities.
 */

package platform.bridge.api.proxy;

import platform.bridge.api.protocol.AbstractPacket;
import platform.core.api.exception.BridgeException;

/**
 * Service proxy base defines network base implementation independent service proxy capabilities.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IServiceProxyBase
{
    /**
     * Attempt to send a response via network base implementation to a client.
     * 
     * @param packet
     *            - a {@link AbstractPacket} to send.
     * @param clientIdentifier
     *            - a {@link String} client identifier to send the response to.
     * @throws BridgeException
     *             - throws {@link BridgeException} on send error.
     */
    void sendPacket(AbstractPacket packet, String clientIdentifier) throws BridgeException;

    /**
     * Release a service proxy and cleanup.
     * 
     * @throws BridgeException
     *             - throws {@link BridgeException} on proxy release error.
     */
    void release() throws BridgeException;

    /**
     * Initialize a service proxy.
     * 
     * @param servicePort
     *            - a {@link Integer} service port to bind on.
     * @param responseListener
     *            - a {@link IResponseListener} listener for client requests. Provides a chain to retrieve specific
     *            proxy implementation connection settings.
     * @throws BridgeException
     *             - throws {@link BridgeException} on proxy initialization error.
     */
    void initialize(Integer servicePort, IResponseListener responseListener) throws BridgeException;
}
