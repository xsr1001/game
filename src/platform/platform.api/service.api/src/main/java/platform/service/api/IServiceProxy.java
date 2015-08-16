/**
 * @file IServiceProxy.java
 * @brief Interface defining basic service proxy functionality.
 */

package platform.service.api;

import java.net.InetAddress;

import platform.core.api.exception.BridgeException;

/**
 * Interface defining basic service proxy functionality. Deriving from this interface allows for registration and
 * initialization of service proxies, providing an active transport channel with a remote service. Interface provides
 * basic proxy management capabilities.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IServiceProxy
{
    // Test service proxy service type.
    public static final String SERVICE_TEST = "TEST-SERVICE";

    /**
     * Initialize service proxy by providing remote service address. Initialization should attempt to establish a
     * connection with remote host.
     * 
     * @param serviceAddress
     *            - a {@link InetAddress} providing valid remote service address and port.
     * @throws BridgeException
     *             - throws {@link BridgeException} on initialization error.
     */
    void initialize(InetAddress serviceAddress) throws BridgeException;

    /**
     * Attempt to release the service proxy and clean up.
     * 
     * @throws BridgeException
     *             - throws {@link BridgeException} on service proxy release error.
     */
    void release() throws BridgeException;
}
