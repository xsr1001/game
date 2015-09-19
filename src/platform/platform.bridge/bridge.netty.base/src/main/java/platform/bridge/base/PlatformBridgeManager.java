/**
 * @file PlatformBridgeManager.java
 * @brief PlatformBridgeManager provides functionality for registering service and client proxies on netty network base..
 */

package platform.bridge.base;

import game.core.util.ArgsChecker;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.base.pipeline.PlatformPipelineInitializer;
import platform.bridge.base.proxy.AbstractNettyBridgeAdapter;
import platform.bridge.base.util.PlatformBridgeUtil;
import platform.core.api.exception.BridgeException;

/**
 * Platform Bridge manager. Provides consumers with functionality for registering either a service or client proxy with
 * their respective protocol stacks on netty network base. Once a proxy has been registered the network socket is opened
 * and data transmission with remote platform end-point can begin.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformBridgeManager extends AbstractBridgeProvider
{
    // Errors, messages, args.
    private static final String ERROR_PROXY_REGISTERED = "%s proxy: [%s] has already been registered.";
    private static final String PROXY_REGISTER1 = "Service";
    private static final String PROXY_REGISTER2 = "Client";
    private static final String MSG_NEW_PROXY = "Registering new %s proxy: [%s] to host [%s].";
    private static final String ARG_SERVICE_PROXY = "serviceProxy";
    private static final String ARG_CLIENT_PROXY = "clientProxy";
    private static final String ARG_SERVICE_PORT = "servicePort";
    private static final String ARG_PROXY = "proxy";
    private static final String ARG_PORT = "port";
    private static final String ARG_REMOTE_PORT = "remoteHostPort";
    private static final String ARG_REMOTE_HOST = "remoteHostIPv4";
    private static final String ARG_BRIDGE_OPTIONS = "bridgeOptions";
    private static final String ARG_LOCALHOST = "localhost";

    // Singleton instance.
    private static final PlatformBridgeManager INSTANCE = new PlatformBridgeManager();

    // Set of registered proxies.
    private Set<String> proxySet;

    // Guard the proxySet.
    private ReentrantReadWriteLock rwLock;

    /**
     * Singleton instance getter.
     * 
     * @return instance of {@link PlatformBridgeManager}.
     */
    public static PlatformBridgeManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private PlatformBridgeManager()
    {
        super();
        proxySet = new HashSet<String>();
        rwLock = new ReentrantReadWriteLock();
    }

    /**
     * Register a proxy with platform bridge manager to either start a service or connect with a remote service.
     * 
     * @param proxy
     *            - a {@link AbstractNettyBridgeAdapter} proxy implementation.
     * @param port
     *            - a valid network port to either connect with or bind on.
     * @param remoteHostIPv4
     *            - a {@link String} remote service connection IPv$ to connect to or null if service proxy.
     * @param bridgeOptions
     *            - an instance of {@link BridgeOptions} to provide additional bridge related parameters.
     * @return - an {@link Integer} local channel network port. Either an actual network port a service has bound on or
     *         a client local channel port.
     * @throws BridgeException
     *             - throws {@link BridgeException} on proxy registration error.
     */
    public synchronized Integer registerProxy(AbstractNettyBridgeAdapter proxy, int port, String remoteHostIPv4,
        BridgeOptions bridgeOptions) throws BridgeException
    {
        ArgsChecker.errorOnNull(proxy, ARG_PROXY);
        ArgsChecker.errorOnNull(bridgeOptions, ARG_BRIDGE_OPTIONS);
        ArgsChecker.errorOnLessThan0(port, ARG_PORT);
        PlatformBridgeUtil.validateBridgeOptions(bridgeOptions, null);

        if ((Boolean) bridgeOptions.get(BridgeOptions.KEY_IS_SERVER).get())
        {
            return registerServiceProxy(proxy, port, bridgeOptions);
        }
        else
        {
            return registerClientProxy(proxy, port, remoteHostIPv4, bridgeOptions);
        }
    }

    /**
     * Attempt to register service proxy and start the service.
     * 
     * @param serviceProxy
     *            - an {@link AbstractNettyBridgeAdapter} service data end-point which serves as a data sink and
     *            provides service specific protocol stack.
     * @param servicePort
     *            - a network port to bind the service on. 0 represents a wild-card port.
     * @param channelOptions
     *            - an instance of {@link BridgeOptions} to provide additional bridge related parameters.
     * @return - a local service channel network port. Represents an actual port the service is registered on. May
     *         differ from provided servicePort if it was a wild-card port.
     * @throws BridgeException
     *             - throw {@link BridgeException} on service registration or server socket bind error.
     */
    private Integer registerServiceProxy(AbstractNettyBridgeAdapter serviceProxy, int servicePort,
        BridgeOptions bridgeOptions) throws BridgeException
    {
        LOG.enterMethod(ARG_SERVICE_PROXY, serviceProxy, ARG_SERVICE_PORT, servicePort, ARG_BRIDGE_OPTIONS,
            bridgeOptions);
        PlatformBridgeUtil.validateBridgeOptions(bridgeOptions, Boolean.TRUE);

        // Check if given proxy has already been registered.
        boolean contains = false;
        try
        {
            rwLock.readLock().lock();
            contains = proxySet.contains(serviceProxy.getName());
        }
        finally
        {
            rwLock.readLock().unlock();
        }

        if (contains)
        {
            LOG.error(String.format(ERROR_PROXY_REGISTERED, PROXY_REGISTER1, serviceProxy));
            throw new BridgeException(String.format(ERROR_PROXY_REGISTERED, PROXY_REGISTER1, serviceProxy));
        }

        LOG.info(String.format(MSG_NEW_PROXY, PROXY_REGISTER1, serviceProxy.getName(),
            ARG_LOCALHOST.concat(":").concat(Integer.toString(servicePort))));

        // Attempt to create the whole service stack and bind the service end-point.
        servicePort = provideServiceBridge(servicePort, new PlatformPipelineInitializer(serviceProxy), bridgeOptions);

        try
        {
            rwLock.writeLock().lock();

            // Prevent multiple proxy registrations.
            proxySet.add(serviceProxy.getName());
        }
        finally
        {
            rwLock.writeLock().unlock();
        }
        LOG.exitMethod(ARG_PORT, servicePort);
        return servicePort;
    }

    /**
     * Attempt to register a client proxy and connect with remote host.
     * 
     * @param clientProxy
     *            - an {@link AbstractNettyBridgeAdapter} client proxy implementation which serves as a data sink and
     *            provides client specific protocol stack.
     * @param remoteHostPort
     *            - a network port of remote host client is trying to connect to.
     * @param remoteHostIPv4
     *            - a network IPv4 of remote host client is trying to connect to.
     * @param bridgeOptions
     *            - an instance of {@link BridgeOptions} to provide additional bridge related options.
     * @return - a local client channel network port. Represents an actual port of the client socket that has
     *         established a connection with a remote host. Usually usage of this port is redundant.
     * @throws BridgeException
     *             - throw {@link BridgeException} on client proxy registration or connection error.
     */
    private Integer registerClientProxy(AbstractNettyBridgeAdapter clientProxy, int remoteHostPort,
        String remoteHostIPv4, BridgeOptions bridgeOptions) throws BridgeException
    {
        LOG.enterMethod(ARG_CLIENT_PROXY, clientProxy, ARG_REMOTE_PORT, remoteHostPort, ARG_REMOTE_HOST,
            remoteHostIPv4, ARG_BRIDGE_OPTIONS, bridgeOptions);
        ArgsChecker.errorOnNull(remoteHostIPv4, ARG_REMOTE_HOST);
        PlatformBridgeUtil.validateBridgeOptions(bridgeOptions, Boolean.FALSE);

        // Check if given proxy has already been registered.
        boolean contains = false;
        try
        {
            rwLock.readLock().lock();
            contains = proxySet.contains(clientProxy.getName());
        }
        finally
        {
            rwLock.readLock().unlock();
        }

        if (contains)
        {
            LOG.error(String.format(ERROR_PROXY_REGISTERED, PROXY_REGISTER2, clientProxy));
            throw new BridgeException(String.format(ERROR_PROXY_REGISTERED, PROXY_REGISTER2, clientProxy));
        }

        LOG.info(String.format(MSG_NEW_PROXY, PROXY_REGISTER2, clientProxy.getName(),
            remoteHostIPv4.concat(":").concat(Integer.toString(remoteHostPort))));

        // Attempt to create the whole client stack and connect with remote host end-point.
        int clientLocalPort = provideClientBridge(new InetSocketAddress(remoteHostIPv4, remoteHostPort),
            new PlatformPipelineInitializer(clientProxy), bridgeOptions);

        try
        {
            rwLock.writeLock().lock();

            // Add now to prevent multiple proxy registrations before registration result.
            proxySet.add(clientProxy.getName());
        }
        finally
        {
            rwLock.writeLock().unlock();
        }
        LOG.exitMethod(ARG_PORT, clientLocalPort);
        return clientLocalPort;
    }

    /**
     * Unregister a proxy.
     * 
     * @param proxy
     *            - a {@link AbstractNettyBridgeAdapter} proxy to unregister.
     * @throws BridgeException
     *             - throws {@link BridgeException} on unregister error.
     */
    public synchronized void unregisterProxy(AbstractNettyBridgeAdapter proxy) throws BridgeException
    {
        ArgsChecker.errorOnNull(proxy, ARG_PROXY);
        try
        {
            rwLock.writeLock().lock();
            proxySet.remove(proxy.getName());
        }
        finally
        {
            rwLock.writeLock().unlock();
        }
    }
}
