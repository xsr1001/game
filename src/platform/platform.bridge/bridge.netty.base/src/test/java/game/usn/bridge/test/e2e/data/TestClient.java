/**
 * @file TestClient.java
 * @brief Simple test client.
 */

package game.usn.bridge.test.e2e.data;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.base.proxy.AbstractBridgeAdapter;

/**
 * Simple test client.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestClient extends AbstractBridgeAdapter implements IChannelObserver
{
    // Testing channel up.
    public boolean channelUp;
    public int observableCallbackCnt;

    // Testing client connection.
    public Channel remoteService;
    public boolean clientConnected;
    public int observableClientCallbackCnt;

    // Testing client send.
    public boolean sent;
    public int sendCallbackCnt;
    public boolean response;

    /**
     * Ctor.
     */
    public TestClient()
    {
        super(new TestServiceProtocol());

        this.channelUp = false;
        this.observableCallbackCnt = 0;

        this.clientConnected = false;
        this.observableClientCallbackCnt = 0;

        this.sent = false;
        this.sendCallbackCnt = 0;
        this.response = false;
    }

    @Override
    public void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        this.channelUp = true;
        this.observableCallbackCnt++;
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
        this.remoteService = ctx.channel();
        this.clientConnected = true;
        this.observableClientCallbackCnt++;

        ctx.channel().writeAndFlush(new PingPacket()).addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                sendCallbackCnt++;
                sent = future.isSuccess();

            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        this.remoteService = ctx.channel();
        this.clientConnected = false;
        this.observableClientCallbackCnt++;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof PongPacket)
        {
            this.response = true;
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
