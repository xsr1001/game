/**
 * @file AbstractDataProxy.java
 * @brief <description>
 */

package game.usn.bridge.proxy;

import game.core.util.ArgsChecker;
import game.usn.bridge.USNBridgeManager;
import game.usn.bridge.api.listener.IChannelObserver;
import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import game.usn.bridge.pipeline.ChannelOptions;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.core.api.exception.BridgeException;

public abstract class AbstractDataProxy extends ChannelInboundHandlerAdapter implements IChannelObserver
{
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";

    // A flag determining if data proxy is initialized.
    private AtomicBoolean initialized;

    // A flag determining if channel is active (either socket has connected or server socket was bound).
    private AtomicBoolean channelActive;

    /**
     * Constructor.
     */
    protected AbstractDataProxy()
    {
        initialized = new AtomicBoolean();
        channelActive = new AtomicBoolean();
    }

    /**
     * Initialize data proxy by registering it with network base.
     * 
     * @param serviceIPv4Address
     *            - a {@link String} service IPv4 address to connect with.
     * @param servicePort
     *            - a {@link Integer} service port to connect to.
     * @throws BridgeException
     *             - throws {@link BridgeException} on data proxy initialization failure.
     */
    protected void initialize(String serviceIPv4Address, Integer servicePort) throws BridgeException
    {
        ArgsChecker.errorOnNull(getChannelOptions(), ARG_CHANNEL_OPTIONS);

        if (!initialized.get())
        {
            if (getChannelOptions().isServer())
            {
                USNBridgeManager.getInstance().registerServiceProxy(this,
                    new HashSet<IChannelObserver>(Arrays.asList(new IChannelObserver[] { this })),
                    servicePort != null ? servicePort : null, getChannelOptions());
            }
            else
            {
                USNBridgeManager.getInstance().registerClientProxy(this,
                    new HashSet<IChannelObserver>(Arrays.asList(new IChannelObserver[] { this })), servicePort,
                    serviceIPv4Address, getChannelOptions());
            }
            initialized.set(true);
        }
    }

    /**
     * Release the connection with remote service and unregister proxy with network base.
     * 
     * @throws BridgeException
     *             - throws {@link BridgeException} on release failure.
     */
    protected void release() throws BridgeException
    {
        if (initialized.get())
        {
            // TODO: unregister a proxy from network base.
            initialized.set(false);
            active.set(false);
        }
    }

    @Override
    public final void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        active.set(true);
    }

    @Override
    public final void notifyChannelDown(String proxyName)
    {
        active.set(false);
    }

    /**
     * Force implementation to provide unique proxy name.
     * 
     * @return - a {@link String} proxy name.
     */
    public abstract String getName();

    /**
     * Retrieve the p
     * 
     * @return
     */
    public abstract AbstractUSNProtocol getProtocol();

    /**
     * Provide default list of proxy in handlers. Specific proxy implementations may override this method to provide
     * proxy specific handlers.
     * 
     * @return - a {@link List} of {@link ChannelHandler} objects, representing default proxy and custom proxy in
     *         handlers.
     */
    public abstract List<ChannelHandler> getInHandlerList();

    public abstract List<ChannelHandler> getOutHandlerList();

    /**
     * Retrieve proxy implementation specific channel options.
     * 
     * @return - a {@link ChannelOptions} object, defining basic options to initialize network channel with.
     */
    protected abstract ChannelOptions getChannelOptions();

    /**
     * Proxy implementation specific logging.
     */
    @Override
    public abstract String toString();
}
