/**
 * @file TestService.java
 * @brief <description>
 */

package game.usn.bridge.test.data;

import game.usn.bridge.api.listener.IChannelObserver;
import game.usn.bridge.api.proxy.AbstractDataProxy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class TestService extends AbstractDataProxy implements IChannelObserver
{

    public boolean channelup = false;
    public Channel client = null;
    public boolean connected = false;
    public int channelCnt = 0;
    public int connectedCnt = 0;
    public int port;

    public TestService()
    {
        super(new TestServiceProtocol());
    }

    @Override
    public void notifyChannelUp(String proxyName, int port)
    {
        channelup = true;
        this.port = port;
        channelCnt++;
    }

    @Override
    public void notifyChannelDown(String proxyName)
    {
        channelup = false;
        channelCnt++;

    }

    @Override
    public void notifyChannelError(String proxyName)
    {
        channelup = false;
        channelCnt++;

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        this.client = ctx.channel();
        this.connected = true;
        this.connectedCnt++;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        this.client = ctx.channel();
        this.connected = false;
        this.connectedCnt++;
    }
}
