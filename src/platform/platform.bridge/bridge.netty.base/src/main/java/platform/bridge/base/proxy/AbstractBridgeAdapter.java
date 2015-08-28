/**
 * @file AbstractBridgeAdapter.java
 * @brief Abstract bridge adapter provides basic bridge infrastructure for platform proxies to use.
 */

package platform.bridge.base.proxy;

import game.core.util.ArgsChecker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.base.PlatformBridgeManager;
import platform.bridge.base.pipeline.ChannelOptions;
import platform.core.api.exception.BridgeException;

/**
 * Abstract bridge adapter provides basic bridge infrastructure for platform proxies to use. It is a bridge layer
 * binding for netty network base.
 * 
 * <p>
 * Abstract bridge adapter is a bridge level adapter, providing binfing with netty network base, defined in
 * {@link ChannelInboundHandlerAdapter}.
 * </p>
 * <p>
 * Abstract bridge adapter enforces a proxy to implement basic channel management logic, defined in
 * {@link IChannelObserver}.
 * </p>
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractBridgeAdapter extends ChannelInboundHandlerAdapter implements IChannelObserver
{
    // Args, messages, errors.
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";

    // A flag determining whether data proxy has been initialized or not.
    private AtomicBoolean initialized;

    /**
     * Constructor.
     */
    protected AbstractBridgeAdapter()
    {
        initialized = new AtomicBoolean();
    }

    /**
     * Initialize data proxy by registering it with network base.
     * 
     * @param serviceIPv4Address
     *            - a {@link String} service IPv4 address to register data proxy with. Required only if registering a
     *            client proxy.
     * @param servicePort
     *            - a {@link Integer} service port to register data proxy with.
     * @throws BridgeException
     *             - throws {@link BridgeException} on data proxy initialization failure.
     */
    public void initialize(String serviceIPv4Address, Integer servicePort) throws BridgeException
    {
        ArgsChecker.errorOnNull(getChannelOptions(), ARG_CHANNEL_OPTIONS);

        // Add self as channel observer to receive channel life-cycle events.
        Set<IChannelObserver> channelObserverSet = new HashSet<IChannelObserver>();
        if (getChannelObserverSet() != null)
        {
            channelObserverSet.addAll(getChannelObserverSet());
        }
        channelObserverSet.add(this);

        if (!initialized.get())
        {
            if (getChannelOptions().isServer())
            {
                PlatformBridgeManager.getInstance().registerServiceProxy(this, channelObserverSet, servicePort,
                    getChannelOptions());
            }
            else
            {
                PlatformBridgeManager.getInstance().registerClientProxy(this, channelObserverSet, servicePort,
                    serviceIPv4Address, getChannelOptions());
            }
            initialized.set(true);
        }
    }

    /**
     * Release data proxy by unregistering it with network base.
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
     * {@inheritDoc} Callback for channel life-cycle event. A connection with a remote service has been established, or
     * a server socket channel has been successfully bound.
     */
    @Override
    public abstract void notifyChannelUp(String proxyName, InetSocketAddress address);

    /**
     * {@inheritDoc} Callback for channel life-cycle event. Client has disconnected with remote service, or a server
     * socket channel has been closed.
     */
    @Override
    public abstract void notifyChannelDown(String proxyName);

    /**
     * Retrieve proxy specific channel options.
     * 
     * @return - a {@link ChannelOptions} object, defining basic options to initialize network channel with.
     */
    protected abstract ChannelOptions getChannelOptions();

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
     * Provide proxy specific list of in handlers.
     * 
     * @return - a {@link List} of {@link ChannelHandler} objects, representing custom proxy specific in handlers.
     */
    public abstract List<ChannelHandler> getInHandlerList();

    /**
     * Provide proxy specific list of out handlers.
     * 
     * @return - a {@link List} of {@link ChannelHandler} objects, representing custom proxy specific out handlers.
     */
    public abstract List<ChannelHandler> getOutHandlerList();

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
