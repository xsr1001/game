/**
 * @file AbstractNettyBridgeAdapter.java
 * @brief Abstract netty bridge adapter provides basic bridge infrastructure for platform proxies to use on top of 
 * netty network base implementation.
 */

package platform.bridge.base.proxy;

import game.core.util.ArgsChecker;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.BridgeOptions;
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
public abstract class AbstractNettyBridgeAdapter extends ChannelInboundHandlerAdapter implements IChannelObserver
{
    // Args, messages, errors.
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";

    // A flag determining whether data proxy has been initialized or not.
    private AtomicBoolean initialized;

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
    public void initialize(String serviceIPv4Address, Integer servicePort) throws BridgeException
    {
        ArgsChecker.errorOnNull(getBridgeOptions(), ARG_CHANNEL_OPTIONS);
        PlatformBridgeUtil.validateBridgeOptions(getBridgeOptions(), null);

        // Add self as channel observer to receive channel life-cycle events.
        Set<IChannelObserver> channelObserverSet = new HashSet<IChannelObserver>();
        if (getChannelObserverSet() != null)
        {
            channelObserverSet.addAll(getChannelObserverSet());
        }
        channelObserverSet.add(this);

        if (!initialized.get())
        {
            if ((Boolean) getBridgeOptions().get(BridgeOptions.KEY_IS_SERVER).get())
            {
                PlatformBridgeManager.getInstance().registerServiceProxy(this, channelObserverSet, servicePort,
                    getBridgeOptions());
            }
            else
            {
                PlatformBridgeManager.getInstance().registerClientProxy(this, channelObserverSet, servicePort,
                    serviceIPv4Address, getBridgeOptions());
            }
            initialized.set(true);
        }
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
     * Retrieve proxy specific bridge options.
     * 
     * @return - a {@link BridgeOptions} object, defining basic options to initialize network channel with.
     */
    protected abstract BridgeOptions getBridgeOptions();

    /**
     * Retrieve proxy implementation specific name.
     * 
     * @return - a {@link String} proxy name.
     */
    public abstract String getName();

    /**
     * Retrieve the proxy specific platform protocol.
     * 
     * @return - an instance of {@link AbstractPlatformProtocol}.
     */
    public abstract AbstractPlatformProtocol getProtocol();

    /**
     * Retrieve proxy specific channel observer set.
     * 
     * @return - a {@link List} of {@link IChannelObserver} objects, representing observers to network channel
     *         life-cycle events.
     */
    public abstract Set<IChannelObserver> getChannelObserverSet();

    @Override
    public final String toString()
    {
        return getName();
    }
}
