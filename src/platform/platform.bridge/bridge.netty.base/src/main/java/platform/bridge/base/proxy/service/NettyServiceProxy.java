/**
 * @file NettyServiceProxy.java
 * @brief Netty service proxy defines netty specific service proxy functionality.
 */

package platform.bridge.base.proxy.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.api.proxy.IResponseListener;
import platform.bridge.api.proxy.IServiceProxyBase;
import platform.bridge.base.proxy.AbstractNettyBridgeAdapter;
import platform.core.api.exception.BridgeException;

/**
 * Netty service proxy defines netty specific service proxy functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class NettyServiceProxy extends AbstractNettyBridgeAdapter implements IServiceProxyBase
{
    // Errors, args, messages.
    private static final String ERROR_MSG_SEND = "Cannot send response to client as channel is not connected.";

    // A flag determining if channel is active (server socket channel has been bound).
    private AtomicBoolean channelBound;

    // Client channel map.
    private Map<String, Channel> clientChannelMap;

    // Response listener for receiving service responses.
    private IResponseListener responseListener;

    /**
     * Constructor.
     */
    public NettyServiceProxy()
    {
        super();
        clientChannelMap = new HashMap<String, Channel>();
        channelBound = new AtomicBoolean();
    }

    @Override
    public void initialize(Integer servicePort, IResponseListener responseListener) throws BridgeException
    {
        super.initialize(null, servicePort);
        this.responseListener = responseListener;
    }

    @Override
    public void release() throws BridgeException
    {
        super.release();
        for (Channel channel : clientChannelMap.values())
        {
            channel.close();
        }
    }

    @Override
    public final void sendPacket(AbstractPacket packet, String senderIdentifier) throws BridgeException
    {
        Channel ch = null;
        ch = clientChannelMap.get(senderIdentifier);
        if (ch != null)
        {
            ch.write(packet).addListener(new ChannelFutureListener() {
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
        else
        {
            throw new BridgeException(ERROR_MSG_SEND);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        responseListener.receive(AbstractPacket.class.cast(msg), ctx.channel().id().asLongText());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        ctx.channel().flush();
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        clientChannelMap.put(ctx.channel().id().asLongText(), ctx.channel());
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        clientChannelMap.remove(ctx.channel().id().asLongText());
    }

    @Override
    public final void notifyChannelDown(String proxyName)
    {
        if (proxyName.compareTo(getName()) == 0)
        {
            channelBound.set(false);
        }
        responseListener.notifyChannelDown(proxyName);
    }

    @Override
    public final void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        channelBound.set(true);
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
