/**
 * @file AbstractNettyBridgeAdapter.java
 * @brief Abstract netty bridge adapter provides basic bridge infrastructure for platform proxies to use on top of 
 * netty network base implementation.
 */

package platform.bridge.base.proxy;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.bridge.api.observer.IChannelObserver;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.api.proxy.IResponseListener;
import platform.bridge.base.PlatformBridgeManager;
import platform.bridge.base.util.PlatformBridgeUtil;
import platform.core.api.exception.BridgeException;

/**
 * Abstract bridge adapter provides basic bridge infrastructure for platform proxies to use on top of netty network base
 * implementation. It is a bridge layer binding for netty network base.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractNettyBridgeAdapter extends ChannelInboundHandlerAdapter
{
    // Logger.
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractNettyBridgeAdapter.class);

    // Args, messages, errors.
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";
    private static final String WARN_CHANNEL_OBSERVER_NOTIFY = "Error notifying channel observer with channel life-cycle change event.";

    // A flag determining whether data proxy has been initialized or not.
    private AtomicBoolean initialized;

    // Upstream response listener.
    protected IResponseListener responseListener;

    /**
     * Constructor.
     */
    protected AbstractNettyBridgeAdapter()
    {
        initialized = new AtomicBoolean();
    }

    /**
     * Initialize netty bridge adapter by registering it with netty network base.
     * 
     * @param serviceIPv4Address
     *            - a {@link String} service IPv4 address to register netty bridge adapter with. Required only if
     *            registering a client proxy.
     * @param servicePort
     *            - a {@link Integer} service port to register netty bridge adapter with.
     * @throws BridgeException
     *             - throws {@link BridgeException} on netty bridge adapter initialization failure.
     */
    public Integer initialize(String serviceIPv4Address, Integer servicePort) throws BridgeException
    {
        ArgsChecker.errorOnNull(getBridgeOptions(), ARG_CHANNEL_OPTIONS);
        PlatformBridgeUtil.validateBridgeOptions(getBridgeOptions(), null);

        Integer localPort = null;

        if (!initialized.get())
        {
            localPort = PlatformBridgeManager.getInstance().registerProxy(this, servicePort, serviceIPv4Address,
                getBridgeOptions());
            initialized.set(true);
        }

        return localPort;
    }

    /**
     * Release netty bridge adapter by unregistering it with network base.
     * 
     * @throws BridgeException
     *             - throws {@link BridgeException} on release failure.
     */
    protected void release() throws BridgeException
    {
        if (initialized.get())
        {
            PlatformBridgeManager.getInstance().unregisterProxy(this);
            initialized.set(false);
        }
    }

    /**
     * Provides proxy specific bridge options.
     * 
     * @return - a {@link BridgeOptions} object, defining basic network and application level parameters.
     */
    public abstract BridgeOptions getBridgeOptions();

    /**
     * Provides proxy implementation specific name. Must be unique per proxy.
     * 
     * @return - a {@link String} unique proxy name.
     */
    public abstract String getName();

    /**
     * Provides proxy specific platform protocol.
     * 
     * @return - an instance of {@link AbstractPlatformProtocol}.
     */
    public abstract AbstractPlatformProtocol getProtocol();

    /**
     * Helper method to notify channel life-cycle observers with received channel life-cycle change events.
     * 
     * @param channelObserverSet
     *            - a {@link List} of {@link IChannelObserver} observers to notify.
     * @param isChannelUp
     *            - a channel life-cycle state change.
     * @param inetSocketAddress
     *            - a {@link InetSocketAddress} socket address associated with this channel state change.
     */
    protected final void notifyChannelLifecycleEvent(Set<IChannelObserver> channelObserverSet, boolean isChannelUp,
        InetSocketAddress inetSocketAddress)
    {
        // Guard for application level stupidity.
        try
        {
            if (channelObserverSet != null)
            {
                for (IChannelObserver channelObserver : channelObserverSet)
                {
                    channelObserver.notifyChannelStateChanged(isChannelUp, getName(), inetSocketAddress);
                }
            }
        }
        catch (Exception e)
        {
            LOG.warn(WARN_CHANNEL_OBSERVER_NOTIFY, e);
        }
    }

    @Override
    public final String toString()
    {
        return getName();
    }
}
