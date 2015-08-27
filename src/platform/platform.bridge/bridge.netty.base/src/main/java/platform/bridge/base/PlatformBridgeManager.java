/**
 * @file PlatformBridgeManager.java
 * @brief PlatformBridgeManager provides functionality for registering service and client proxies on platform. 
 */

package platform.bridge.base;

import game.core.util.ArgsChecker;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.base.pipeline.ChannelOptions;
import platform.bridge.base.pipeline.PlatformPipelineInitializer;
import platform.bridge.base.proxy.AbstractBridgeAdapter;
import platform.bridge.base.util.PlatformBridgeUtil;
import platform.core.api.exception.BridgeException;

/**
 * Platform Bridge manager. Provides consumers with functionality for registering either service or client proxies with
 * their respective protocol stacks. Once a proxy has been registered the network socket is opened and data transmission
 * with remote platform end-point can begin.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformBridgeManager extends AbstractBridgeProvider implements IChannelObserver
{
    // Errors, messages, args.
    private static final String ERROR_PROXY_REGISTERED = "%s proxy: [%s] has already been registered.";
    private static final String PROXY_REGISTER1 = "Service";
    private static final String PROXY_REGISTER2 = "Client";
    private static final String MSG_NEW_PROXY = "Registering new %s proxy: [%s] to host [%s].";
    private static final String MSG_PROXY_REGISTERED = "Successfully registered new proxy: [%s] on address: [%s].";
    private static final String MSG_PROXY_UNREGISTERED = "Successfully unregistered or error with new proxy: [%s].";
    private static final String ARG_SERVICE_PROXY = "serviceProxy";
    private static final String ARG_CLIENT_PROXY = "clientProxy";
    private static final String ARG_SERVICE_PORT = "servicePort";
    private static final String ARG_PROXY = "proxy";
    private static final String ARG_REMOTE_PORT = "remoteHostPort";
    private static final String ARG_REMOTE_HOST = "remoteHostIPv4";
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";
    private static final String ARG_CHANNEL_OBSERVERS = "channelObserversSet";
    private static final String ARG_LOCALHOST = "localhost";

    // Singleton instance.
    private static final PlatformBridgeManager INSTANCE = new PlatformBridgeManager();

    // Set of registered and waiting for registration result proxies.
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
     * Attempt to register service proxy and start the service.
     * 
     * @param serviceProxy
     *            - an {@link AbstractBridgeAdapter} service data end-point which serves as a data sink and provides
     *            service specific protocol stack.
     * @param channelObserverSet
     *            - a {@link Set} of {@link IChannelObserver} objects, containing interested channel observers. Service
     *            channel life-cycle events will be notified to provided observers.
     * @param servicePort
     *            - a network port to bind the service on. 0 represents wild-card port.
     * @param channelOptions
     *            - an instance of {@link ChannelOptions} to provide additional bridge related parameters.
     * @throws BridgeException
     *             - throw {@link BridgeException} on service registration or server socket bind error.
     */
    public synchronized void registerServiceProxy(AbstractBridgeAdapter serviceProxy,
        final Set<IChannelObserver> channelObserverSet, int servicePort, ChannelOptions channelOptions)
        throws BridgeException
    {
        LOG.enterMethod(ARG_SERVICE_PROXY, serviceProxy, ARG_SERVICE_PORT, servicePort, ARG_CHANNEL_OPTIONS,
            channelOptions);

        ArgsChecker.errorOnNull(serviceProxy, ARG_SERVICE_PROXY);
        ArgsChecker.errorOnNull(channelOptions, ARG_CHANNEL_OPTIONS);
        ArgsChecker.errorOnNull(channelObserverSet, ARG_CHANNEL_OBSERVERS);
        ArgsChecker.errorOnLessThan0(servicePort, ARG_SERVICE_PORT);

        PlatformBridgeUtil.validateChannelOptions(channelOptions, true);

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

        LOG.info(MSG_NEW_PROXY, PROXY_REGISTER1, serviceProxy.getName(),
            ARG_LOCALHOST.concat(":").concat(Integer.toString(servicePort)));

        // Add self to observer set to receive actual service channel life-cycle status.
        channelObserverSet.add(this);

        // Attempt to create the whole service stack and bind the service end-point.
        provideServiceBridge(servicePort, new PlatformPipelineInitializer(serviceProxy), channelObserverSet, channelOptions);

        try
        {
            rwLock.writeLock().lock();

            // Add now to prevent multiple proxy registrations before registration result.
            proxySet.add(serviceProxy.getName());
        }
        finally
        {
            rwLock.writeLock().unlock();
        }

        LOG.exitMethod();
    }

    /**
     * Attempt to register a client proxy and connect with remote host.
     * 
     * @param clientProxy
     *            - an {@link AbstractBridgeAdapter} client data end-point which serves as a data sink and provides
     *            client specific protocol stack.
     * @param channelListenerSet
     *            - a {@link Set} of {@link IChannelObserver} objects, containing interested channel observers. Client
     *            channel life-cycle events will be notified to provided listeners.
     * @param remoteHostPort
     *            - a network port of remote host client is trying to connect to.
     * @param remoteHostIPv4
     *            - a network IPv4 of remote host client is trying to connect to.
     * @param channelOptions
     *            - an instance of {@link ChannelOptions} to provide additional bridge related parameters.
     * @throws BridgeException
     *             - throw {@link BridgeException} on client proxy registration or connection error.
     */
    public synchronized void registerClientProxy(AbstractBridgeAdapter clientProxy,
        final Set<IChannelObserver> channelObserverSet, int remoteHostPort, String remoteHostIPv4,
        ChannelOptions channelOptions) throws BridgeException
    {
        LOG.enterMethod(ARG_CLIENT_PROXY, clientProxy, ARG_REMOTE_PORT, remoteHostPort, ARG_REMOTE_HOST,
            remoteHostIPv4, ARG_CHANNEL_OPTIONS, channelOptions);

        ArgsChecker.errorOnNull(clientProxy, ARG_SERVICE_PROXY);
        ArgsChecker.errorOnNull(channelOptions, ARG_CHANNEL_OPTIONS);
        ArgsChecker.errorOnNull(channelObserverSet, ARG_CHANNEL_OBSERVERS);
        ArgsChecker.errorOnNull(remoteHostIPv4, ARG_CHANNEL_OBSERVERS);
        ArgsChecker.errorOnLessThan0(remoteHostPort, ARG_REMOTE_PORT);

        PlatformBridgeUtil.validateChannelOptions(channelOptions, false);

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

        LOG.info(MSG_NEW_PROXY, PROXY_REGISTER2, clientProxy.getName(),
            remoteHostIPv4.concat(":").concat(Integer.toString(remoteHostPort)));

        // Add self to listener set to receive actual client channel life-cycle status.
        channelObserverSet.add(this);

        // Attempt to create the whole client stack and connect with remote host end-point.
        provideClientBridge(new InetSocketAddress(remoteHostIPv4, remoteHostPort), new PlatformPipelineInitializer(
            clientProxy), channelObserverSet, channelOptions);

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

        LOG.exitMethod();
    }

    /**
     * Unregister provided proxy.
     * 
     * @param proxy
     *            - a {@link AbstractBridgeAdapter} proxy to unregister.
     * @throws BridgeException
     *             - throws {@link BridgeException} on unregister error.
     */
    public synchronized void unregisterProxy(AbstractBridgeAdapter proxy) throws BridgeException
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

    @Override
    public void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        LOG.info(String.format(MSG_PROXY_REGISTERED, proxyName, address));
    }

    @Override
    public void notifyChannelDown(String proxyName)
    {
        LOG.info(String.format(MSG_PROXY_UNREGISTERED, proxyName));
        try
        {
            rwLock.writeLock().lock();

            // No need to notify consumer observers, as they should have been notified from internal channel result
            // listener.
            proxySet.remove(proxyName);
        }
        finally
        {
            rwLock.writeLock().unlock();
        }
    }
}
