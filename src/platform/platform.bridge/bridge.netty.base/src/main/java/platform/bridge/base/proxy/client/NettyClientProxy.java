/**
 * @file NettyClientProxy.java
 * @brief Netty client proxy defines netty specific client proxy functionality.
 */

package platform.bridge.base.proxy.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.api.proxy.IClientProxyBase;
import platform.bridge.api.proxy.IResponseListener;
import platform.bridge.base.proxy.AbstractNettyBridgeAdapter;
import platform.core.api.exception.BridgeException;

/**
 * Netty client proxy defines netty specific client proxy functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class NettyClientProxy extends AbstractNettyBridgeAdapter implements IClientProxyBase
{
    // Errors, args, messages.
    private static final String ERROR_MSG_SEND = "Cannot send a message to remote service as channel is not connected.";

    // A flag determining if channel is active (socket has connected).
    private AtomicBoolean channelConnected;

    // Client channel.
    private Channel channel;

    // Response listener for receiving service responses.
    private IResponseListener responseListener;

    /**
     * Constructor.
     */
    public NettyClientProxy()
    {
        super();
        channelConnected = new AtomicBoolean();
    }

    @Override
    public void initialize(String serviceIPv4Address, Integer servicePort, IResponseListener responseListener)
        throws BridgeException
    {
        super.initialize(serviceIPv4Address, servicePort);
        this.responseListener = responseListener;
    }

    @Override
    public void release() throws BridgeException
    {
        super.release();
        channel.disconnect();
    }

    /**
     * Attempt to send a packet through the downstream pipeline to a remote service.
     * 
     * @param packet
     *            - a source {@link AbstractPacket} packet to send.
     * @throws BridgeException
     *             - throws {@link BridgeException} on send failure.
     */
    public final void sendPacket(AbstractPacket packet) throws BridgeException
    {
        if (channelConnected.get())
        {
            channel.writeAndFlush(packet).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws BridgeException
                {
                    if (!future.isSuccess())
                    {
                        throw new BridgeException(ERROR_MSG_SEND, future.cause());
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}. Receive a response and forward it upstream.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        responseListener.receive(AbstractPacket.class.cast(msg), ctx.channel().id().asLongText());
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        channel = ctx.channel();
        channelConnected.set(true);
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        channelConnected.set(false);
    }

    @Override
    public final void notifyChannelDown(String proxyName)
    {
        if (proxyName.compareTo(getName()) == 0)
        {
            channelConnected.set(false);
        }

        responseListener.notifyChannelDown(proxyName);
    }

    @Override
    public final void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        responseListener.notifyChannelUp(proxyName, address);
    }

    @Override
    public BridgeOptions getBridgeOptions()
    {
        return responseListener.getBridgeOptions();
    }

    @Override
    public String getName()
    {
        return responseListener.getName();
    }

    @Override
    public AbstractPlatformProtocol getProtocol()
    {
        return responseListener.getProtocol();
    }

    @Override
    public Set<IChannelObserver> getChannelObserverSet()
    {
        return responseListener.getChannelObserverSet();
    }
}
