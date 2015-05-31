/**
 * @file USNBridgeManager.java
 * @brief USNBridgeManager provided functionality for registering service and client proxies to USN. 
 */

package game.usn.bridge;

import game.core.util.ArgsChecker;
import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.listener.IChannelListener;
import game.usn.bridge.api.proxy.AbstractDataProxy;
import game.usn.bridge.pipeline.ChannelOptions;
import game.usn.bridge.pipeline.USNPipelineInitializer;
import game.usn.bridge.util.USNBridgeUtil;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * USN Bridge manager. Provides consumers with functionality for registering either service or client proxies with their
 * respective protocol stacks. Once a proxy has been registered the network socket is opened and data transmission with
 * remote USN end-point can begin.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class USNBridgeManager extends AbstractBridgeProvider implements IChannelListener
{
    // Errors, messages, args.
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_PROXY_REGISTER = "Error registering new proxy: [%s].";
    private static final String WARN_PROXY_REGISTERED = "%s proxy: [%s] has already been registered.";
    private static final String PROXY_REGISTER1 = "Service";
    private static final String PROXY_REGISTER2 = "Client";
    private static final String MSG_NEW_PROXY = "Registering new %s proxy: [%s] to host [%s].";
    private static final String MSG_PROXY_REGISTERED = "Successfully registered new proxy: [%s] on port: [%d].";
    private static final String MSG_PROXY_UNREGISTERED = "Successfully unregistered new proxy: [%s].";
    private static final String ARG_SERVICE_PROXY = "serviceProxy";
    private static final String ARG_CLIENT_PROXY = "clientProxy";
    private static final String ARG_SERVICE_PORT = "servicePort";
    private static final String ARG_REMOTE_PORT = "remoteHostPort";
    private static final String ARG_REMOTE_HOST = "remoteHostIPv4";
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";
    private static final String ARG_CHANNEL_LISTENERS = "channelListenersSet";
    private static final String ARG_LOCALHOST = "localhost";

    // Singleton instance.
    private static final USNBridgeManager INSTANCE = new USNBridgeManager();

    // Set of registered and waiting for registration result proxies.
    private Set<String> proxySet;

    // As registration results are sent asynchronously guard the proxySet.
    private ReentrantReadWriteLock rwLock;

    /**
     * Singleton instance getter.
     * 
     * @return instance of {@link USNBridgeManager}.
     */
    public static USNBridgeManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private USNBridgeManager()
    {
        super();
        this.proxySet = new HashSet<String>();
        this.rwLock = new ReentrantReadWriteLock();
    }

    /**
     * Attempt to register service proxy and start the service.
     * 
     * @param serviceProxy
     *            - an {@link AbstractDataProxy} service data end-point which serves as a data sink and provides service
     *            specific protocol stack.
     * @param channelListenerSet
     *            - a {@link Set}<{@link IChannelListener}> set of channel listeners. Service channel life-cycle events
     *            will be notified to provided listeners. It is preferred to provide at least one consumer specific
     *            listener to listen for proxy registration results and channel life-cycle events.
     * @param servicePort
     *            - a network port to bind service on. 0 represents wild-card port.
     * @param channelOptions
     *            - an instance of {@link ChannelOptions} to provide additional bridge related parameters.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    public synchronized void registerServiceProxy(AbstractDataProxy serviceProxy,
        final Set<IChannelListener> channelListenerSet, int servicePort, ChannelOptions channelOptions)
        throws BridgeException
    {
        LOG.enterMethod(ARG_SERVICE_PROXY, serviceProxy, ARG_SERVICE_PORT, servicePort, ARG_CHANNEL_OPTIONS,
            channelOptions);

        try
        {
            ArgsChecker.errorOnNull(serviceProxy, ARG_SERVICE_PROXY);
            ArgsChecker.errorOnNull(channelOptions, ARG_CHANNEL_OPTIONS);
            ArgsChecker.errorOnNull(channelListenerSet, ARG_CHANNEL_LISTENERS);
            ArgsChecker.errorOnLessThan0(servicePort, ARG_SERVICE_PORT);

            USNBridgeUtil.validateChannelOptions(channelOptions, true);
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new BridgeException(ERROR_ILLEGAL_ARGUMENT, ie);
        }

        // Check if given proxy has already been registered. Notify listeners if already registered.
        try
        {
            this.rwLock.readLock().lock();
            if (this.proxySet.contains(serviceProxy))
            {
                LOG.warn(String.format(WARN_PROXY_REGISTERED, PROXY_REGISTER1, serviceProxy));
                if (channelListenerSet != null)
                {
                    for (IChannelListener listener : channelListenerSet)
                    {
                        listener.notifyChannelError(serviceProxy.getName());
                    }
                }
                return;
            }
        }
        finally
        {
            this.rwLock.readLock().unlock();
        }

        LOG.info(MSG_NEW_PROXY, PROXY_REGISTER1, serviceProxy.getName(),
            ARG_LOCALHOST.concat(Integer.toString(servicePort)));

        // Add self to listener set to receive actual service channel life-cycle status.
        channelListenerSet.add(this);

        // Attempt to create the whole service stack and bind the service end-point.
        provideServiceBridge(servicePort, new USNPipelineInitializer(serviceProxy), channelListenerSet, channelOptions);

        try
        {
            this.rwLock.writeLock().lock();

            // Add now to prevent multiple proxy registrations before registration result.
            this.proxySet.add(serviceProxy.getName());
        }
        finally
        {
            this.rwLock.writeLock().unlock();
        }

        LOG.exitMethod();
    }

    /**
     * Attempt to register client proxy and connect with remote host.
     * 
     * @param clientProxy
     *            - an {@link AbstractDataProxy} client data end-point which serves as a data sink and provides client
     *            specific protocol stack.
     * @param channelListenerSet
     *            - a {@link Set}<{@link IChannelListener}> set of channel listeners. Service channel life-cycle events
     *            will be notified to provided listeners. It is preferred to provide at least one consumer specific
     *            listener to listen for proxy registration results and channel life-cycle events.
     * @param remoteHostPort
     *            - a network port of remote host client is trying to connect to.
     * @param remoteHostIPv4
     *            - a network ipv4 of remote host client is trying to connect to.
     * @param channelOptions
     *            - an instance of {@link ChannelOptions} to provide additional bridge related parameters.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    public synchronized void registerClientProxy(AbstractDataProxy clientProxy,
        final Set<IChannelListener> channelListenerSet, short remoteHostPort, String remoteHostIPv4,
        ChannelOptions channelOptions) throws BridgeException
    {
        LOG.enterMethod(ARG_CLIENT_PROXY, clientProxy, ARG_REMOTE_PORT, remoteHostPort, ARG_REMOTE_HOST,
            remoteHostIPv4, ARG_CHANNEL_OPTIONS, channelOptions);

        try
        {
            ArgsChecker.errorOnNull(clientProxy, ARG_SERVICE_PROXY);
            ArgsChecker.errorOnNull(channelOptions, ARG_CHANNEL_OPTIONS);
            ArgsChecker.errorOnNull(channelListenerSet, ARG_CHANNEL_LISTENERS);
            ArgsChecker.errorOnNull(remoteHostIPv4, ARG_CHANNEL_LISTENERS);
            ArgsChecker.errorOnLessThan0(remoteHostPort, ARG_REMOTE_PORT);

            USNBridgeUtil.validateChannelOptions(channelOptions, false);
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new BridgeException(ERROR_ILLEGAL_ARGUMENT, ie);
        }

        // Check if given proxy has already been registered. Notify listeners if already registered.
        try
        {
            this.rwLock.readLock().lock();
            if (this.proxySet.contains(clientProxy))
            {
                LOG.warn(String.format(WARN_PROXY_REGISTERED, PROXY_REGISTER2, clientProxy));
                if (channelListenerSet != null)
                {
                    for (IChannelListener listener : channelListenerSet)
                    {
                        listener.notifyChannelError(clientProxy.getName());
                    }
                }
                return;
            }
        }
        finally
        {
            this.rwLock.readLock().unlock();
        }

        LOG.info(MSG_NEW_PROXY, PROXY_REGISTER2, clientProxy.getName(),
            remoteHostIPv4.concat(Short.toString(remoteHostPort)));

        // Add self to listener set to receive actual client channel life-cycle status.
        channelListenerSet.add(this);

        // Attempt to create the whole client stack and connect with remote host end-point.
        provideClientBridge(new InetSocketAddress(remoteHostIPv4, remoteHostPort), new USNPipelineInitializer(
            clientProxy), channelListenerSet, channelOptions);

        try
        {
            this.rwLock.writeLock().lock();

            // Add now to prevent multiple proxy registrations before registration result.
            this.proxySet.add(clientProxy.getName());
        }
        finally
        {
            this.rwLock.writeLock().unlock();
        }

        LOG.exitMethod();
    }

    @Override
    public void notifyChannelUp(String proxyName, int port)
    {
        LOG.info(String.format(MSG_PROXY_REGISTERED, proxyName, port));
    }

    @Override
    public void notifyChannelDown(String proxyName)
    {
        LOG.info(String.format(MSG_PROXY_UNREGISTERED, proxyName));
        try
        {
            this.rwLock.writeLock().lock();
            this.proxySet.remove(proxyName);
        }
        finally
        {
            this.rwLock.writeLock().unlock();
        }
    }

    @Override
    public void notifyChannelError(String proxyName)
    {
        LOG.error(String.format(ERROR_PROXY_REGISTER, proxyName));
        try
        {
            this.rwLock.writeLock().lock();

            // No need to notify consumer listeners, as everyone should have been notified from internal channel result
            // listener.
            this.proxySet.remove(proxyName);
        }
        finally
        {
            this.rwLock.writeLock().unlock();
        }
    }
}
