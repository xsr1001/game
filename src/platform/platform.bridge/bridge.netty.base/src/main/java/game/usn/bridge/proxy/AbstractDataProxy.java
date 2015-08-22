/**
 * @file AbstractDataProxy.java
 * @brief Abstract data proxy defines basic proxy functionality for platform service network base.
 */

package game.usn.bridge.proxy;

import game.core.util.ArgsChecker;
import game.usn.bridge.USNBridgeManager;
import game.usn.bridge.api.listener.IChannelObserver;
import game.usn.bridge.api.protocol.AbstractPacket;
import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import game.usn.bridge.pipeline.ChannelOptions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.core.api.exception.BridgeException;

/**
 * Abstract data proxy defines basic proxy functionality for platform service network base.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractDataProxy extends ChannelInboundHandlerAdapter implements IChannelObserver
{
    // Args, messages, errors.
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";

    // A flag determining if data proxy has been initialized.
    private AtomicBoolean initialized;

    // A flag determining if channel is active (either socket has connected or server socket was bound).
    private AtomicBoolean channelActive;

    private Channel channel;

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
                    new HashSet<IChannelObserver>(Arrays.asList(new IChannelObserver[] { this })), servicePort,
                    getChannelOptions());
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
        }
    }

    /**
     * Attempt to send a packet through the downstream pipeline to a remote service.
     * 
     * @param packet
     *            - a source {@link AbstractPacket} packet to send.
     * @throws BridgeException
     *             - throws {@link BridgeException} on send failure.
     */
    protected final void sendPacket(AbstractPacket packet) throws BridgeException
    {
        channel.writeAndFlush(packet).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws BridgeException
            {
                if (!future.isSuccess())
                {
                    throw new BridgeException("", future.cause());
                }
            }
        });
    }

    /**
     * Receive a response from the remote service.
     * 
     * @param packet
     *            - a {@link AbstractPacket} response packet.
     */
    protected abstract void receive(AbstractPacket packet);

    /**
     * {@inheritDoc}. Receive a response and forward it upstream.
     * 
     * @throws ClassCastException
     *             - throws {@link ClassCastException} on cast failure. This generally should not occur as upstream
     *             protocol handler should handle it.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws ClassCastException
    {
        receive(AbstractPacket.class.cast(msg));
    }

    /**
     * Retrieve proxy implementation specific channel options.
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
     * Retrieve the proxy implementation specific protocol.
     * 
     * @return - an instance of {@link AbstractUSNProtocol}.
     */
    public abstract AbstractUSNProtocol getProtocol();

    /**
     * Provide proxy implementation specific list of in handlers.
     * 
     * @return - a {@link List} of {@link ChannelHandler} objects, representing custom proxy implementation specific in
     *         handlers.
     */
    public abstract List<ChannelHandler> getInHandlerList();

    /**
     * Provide proxy implementation specific list of out handlers.
     * 
     * @return - a {@link List} of {@link ChannelHandler} objects, representing custom proxy implementation specific out
     *         handlers.
     */
    public abstract List<ChannelHandler> getOutHandlerList();

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public final void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        if (proxyName.compareTo(getName()) == 0)
        {
            channelActive.set(true);
        }
    }

    @Override
    public final void notifyChannelDown(String proxyName)
    {
        if (proxyName.compareTo(getName()) == 0)
        {
            channelActive.set(false);
        }
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        channel = ctx.channel();
        ctx.fireChannelActive();
    }
}
