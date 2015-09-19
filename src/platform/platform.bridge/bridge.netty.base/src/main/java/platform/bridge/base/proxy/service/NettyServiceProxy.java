/**
 * @file NettyServiceProxy.java
 * @brief Netty service proxy defines netty specific service proxy functionality.
 */

package platform.bridge.base.proxy.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

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
    private static final String ERROR_MSG_SEND = "Cannot send response to client as channel is not active.";
    private static final String ERROR_UNKNOWN_HOST = "Unknown local host while retrieving local host address.";

    // Client channel map.
    private Map<String, Channel> clientChannelMap;

    // Actual service port this service is listening on.
    private Integer activeServicePort;

    /**
     * Constructor.
     */
    public NettyServiceProxy()
    {
        super();
        clientChannelMap = new HashMap<String, Channel>();
    }

    @Override
    public void initialize(Integer servicePort, IResponseListener responseListener) throws BridgeException
    {
        this.responseListener = responseListener;
        activeServicePort = super.initialize(null, servicePort);

        try
        {
            notifyChannelLifecycleEvent(responseListener.getChannelObserverSet(), Boolean.TRUE, new InetSocketAddress(
                Inet4Address.getLocalHost(), activeServicePort));
        }
        catch (UnknownHostException uke)
        {
            LOG.error(ERROR_UNKNOWN_HOST, uke);
            throw new BridgeException(ERROR_UNKNOWN_HOST, uke);
        }
    }

    @Override
    public void release() throws BridgeException
    {
        super.release();
        for (Channel channel : clientChannelMap.values())
        {
            channel.close();
        }

        try
        {
            notifyChannelLifecycleEvent(responseListener.getChannelObserverSet(), Boolean.FALSE, new InetSocketAddress(
                Inet4Address.getLocalHost(), activeServicePort));
        }
        catch (UnknownHostException uke)
        {
            LOG.error(ERROR_UNKNOWN_HOST, uke);
            throw new BridgeException(ERROR_UNKNOWN_HOST, uke);
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
}
