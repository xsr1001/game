/**
 * @file TestService.java
 * @brief Simple test service.
 */

package game.usn.bridge.test.e2e.data;

import game.usn.bridge.api.listener.IChannelObserver;
import game.usn.bridge.proxy.AbstractBridgeAdapter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

/**
 * Simple test service.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestService extends AbstractBridgeAdapter implements IChannelObserver
{
    // Testing channel up.
    public volatile boolean channelUp;
    public volatile int observableCallbackCnt;
    public volatile int servicePort;

    // Testing client connection.
    public Channel client;
    public volatile boolean clientConnected;
    public volatile int observableClientCallbackCnt;

    public volatile boolean received;
    public volatile boolean sent;
    public volatile int sendCallbackCnt;

    /**
     * Ctor.
     */
    public TestService()
    {
        super(new TestServiceProtocol());

        this.channelUp = false;
        this.observableCallbackCnt = 0;
        this.servicePort = -1;

        this.clientConnected = false;
        this.observableClientCallbackCnt = 0;

        this.received = false;
        this.sent = false;
        this.sendCallbackCnt = 0;
    }

    @Override
    public void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        this.channelUp = true;
        this.observableCallbackCnt++;
        this.servicePort = address.getPort();
    }

    @Override
    public void notifyChannelDown(String proxyName)
    {
        this.channelUp = true;
        this.observableCallbackCnt++;

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        this.client = ctx.channel();
        this.clientConnected = true;
        this.observableClientCallbackCnt++;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        this.client = ctx.channel();
        this.clientConnected = false;
        this.observableClientCallbackCnt++;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof PingPacket)
        {
            this.received = true;
            ctx.channel().writeAndFlush(new PongPacket()).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    sendCallbackCnt++;
                    sent = future.isSuccess();
                }
            });
        }
        else
        {
            throw new RuntimeException();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        throw new RuntimeException();
    }
}
