/**
 * @file USNBridge.java
 * @brief <description>
 */

package game.usn.bridge;

import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.listener.IChannelListener;
import game.usn.bridge.api.listener.IConnectionListener;
import game.usn.bridge.api.proxy.AbstractDataProxy;
import game.usn.bridge.pipeline.ChannelOptions;
import game.usn.bridge.pipeline.USNPipelineInitializer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class USNBridgeManager extends AbstractBridgeProvider implements IChannelListener
{
    // Errors, messages, args.
    private static final String ERROR_PROXY_REGISTERED = "%s proxy: [%s] has already been registered.";
    private static final String PROXY_REGISTER1 = "Service";
    private static final String PROXY_REGISTER2 = "Client";
    private static final String MSG_NEW_PROXY = "Registering new %s proxy: [%s].";
    private static final String ARG_SERVICE_PROXY = "serviceProxy";
    private static final String ARG_SERVICE_PORT = "servicePort";

    // Singleton instance.
    private static final USNBridgeManager INSTANCE = new USNBridgeManager();

    private static final int DEFAULT_READ_TIMEOUT_SEC = 30;

    private Set<AbstractDataProxy> proxySet;

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
        this.proxySet = new HashSet<AbstractDataProxy>();
        this.rwLock = new ReentrantReadWriteLock();
    }

    /**
     * Attempt to register service proxy and start the service.
     * 
     * @param serviceProxy
     *            - a {@link AbstractDataProxy} service data end-point. Provides service specific protocol stack.
     * @param channelListenerSet
     *            - a {@link Set}<{@link IChannelListener}> set of channel listeners. Service channel life-cycle events
     *            will be reported to provided listeners. It is preferred to provide at least one consumer specified
     *            listener to catch proxy registration and channel life-cycle events.
     * @param servicePort
     *            - a network port to bind service on. 0 represents wild-card port.
     * @param externalConnectionListenerSet
     * @throws BridgeException
     */
    public synchronized void registerServiceProxy(AbstractDataProxy serviceProxy,
        final Set<IChannelListener> channelListenerSet, short servicePort,
        final Set<IConnectionListener> externalConnectionListenerSet) throws BridgeException
    {
        registerServiceProxy(serviceProxy, channelListenerSet, servicePort, externalConnectionListenerSet, false,
            DEFAULT_READ_TIMEOUT_SEC);
    }

    /**
     * Attempt to register service proxy and start the service.
     * 
     * @param serviceProxy
     *            - a {@link AbstractDataProxy} service data end-point. Provides service specific protocol stack.
     * @param channelListenerSet
     *            - a {@link Set}<{@link IChannelListener}> set of channel listeners. Service channel life-cycle events
     *            will be reported to provided listeners. It is preferred to provide at least one consumer specified
     *            listener to catch proxy registration and channel life-cycle events.
     * @param servicePort
     *            - a network port to bind service on. 0 represents wild-card port.
     * @param externalConnectionListenerSet
     * @param isSSLEnabled
     * @param readTimeoutSeconds
     * @throws BridgeException
     */
    public synchronized void registerServiceProxy(AbstractDataProxy serviceProxy,
        final Set<IChannelListener> channelListenerSet, short servicePort,
        Set<IConnectionListener> externalConnectionListenerSet, boolean isSSLEnabled, int readTimeoutSeconds)
        throws BridgeException
    {
        LOG.enterMethod(ARG_SERVICE_PROXY, serviceProxy, ARG_SERVICE_PORT, servicePort);

        // Check if given proxy has already been registered. Try to notify listeners if already registered.
        try
        {
            this.rwLock.readLock().lock();
            if (this.proxySet.contains(serviceProxy))
            {
                LOG.error(String.format(ERROR_PROXY_REGISTERED, PROXY_REGISTER1, serviceProxy));
                if (channelListenerSet != null)
                {
                    for (IChannelListener listener : channelListenerSet)
                    {
                        listener.notifyChannelError();
                    }
                }
                return;
            }
        }
        finally
        {
            this.rwLock.readLock().unlock();
        }

        LOG.info(MSG_NEW_PROXY, PROXY_REGISTER1, serviceProxy.getName());

        // Add self to listener set to receive actual channel life-cycle status.
        channelListenerSet.add(this);

        // Attempt to create the whole service stack and bind the service end-point.
        provideServiceBridge(servicePort, new USNPipelineInitializer(serviceProxy), channelListenerSet,
            new ChannelOptions(isSSLEnabled, readTimeoutSeconds, true, new HashSet<IConnectionListener>(
                externalConnectionListenerSet)));

        LOG.exitMethod();
    }

    @Override
    public void notifyChannelUp()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyChannelDown()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyChannelError()
    {
        // TODO Auto-generated method stub

    }
}
