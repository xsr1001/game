/**
 * @file AbstractClientProxy.java
 * @brief Abstract client proxy defines basic client proxy functionality.
 */

package platform.bridge.proxy.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.base.proxy.AbstractBridgeAdapter;
import platform.core.api.exception.BridgeException;

/**
 * Abstract client proxy defines basic client proxy functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractClientProxy extends AbstractBridgeAdapter
{
    // Errors, args, mesages.
    private static final String ERROR_MSG_SEND = "Cannot send a message to remote service as channel is not connected.";

    // A flag determining if channel is active (socket has connected).
    private AtomicBoolean channelConnected;

    // Client channel.
    private Channel channel;

    /**
     * Constructor.
     */
    protected AbstractClientProxy()
    {
        super();
        channelConnected = new AtomicBoolean();
    }

    @Override
    protected void release() throws BridgeException
    {
        super.release();
        channel.close();
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
     * Receive a response from the remote service.
     * 
     * @param packet
     *            - a {@link AbstractPacket} response packet.
     */
    protected abstract void receive(AbstractPacket packet);

    /**
     * {@inheritDoc}. Receive a response and forward it upstream.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        super.channelRead(ctx, msg);
        receive(AbstractPacket.class.cast(msg));
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive(ctx);
        channel = ctx.channel();
        channelConnected.set(true);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        channelConnected.set(false);
    }

    @Override
    public void notifyChannelDown(String proxyName)
    {
        if (proxyName.compareTo(getName()) == 0)
        {
            channelConnected.set(false);
        }
    }

    @Override
    public final void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        // No need to implement as channelActive() is enough for client proxy.
    }
}
