/**
 * @file AbstractDataProxy.java
 * @brief Abstract data proxy defines basic proxy functionality for platform service network base.
 */

package game.usn.bridge.proxy;

import game.usn.bridge.api.listener.IChannelObserver;
import game.usn.bridge.api.protocol.AbstractPacket;
import game.usn.bridge.proxy.AbstractBridgeAdapter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import platform.core.api.exception.BridgeException;

/**
 * Abstract data proxy defines basic proxy functionality for platform service network base.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractClientProxy extends AbstractBridgeAdapter implements IChannelObserver
{
    // Args, messages, errors.
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";

    // A flag determining if channel is active (either socket has connected or server socket was bound).
    private AtomicBoolean channelActive;

    private Channel channel;

    /**
     * Constructor.
     */
    protected AbstractClientProxy()
    {
        super();
        channelActive = new AtomicBoolean();
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
