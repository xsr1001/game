/**
 * @file AbstractDataProxy.java
 * @brief <description>
 */

package game.usn.bridge.api.proxy;

import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractDataProxy extends ChannelInboundHandlerAdapter
{
    private List<ChannelHandler> inHandlers;
    private List<ChannelHandler> outHandlers;

    private AbstractUSNProtocol consumerProtocol;

    protected AbstractDataProxy(AbstractUSNProtocol consumerProtocol)
    {
        this.inHandlers = new LinkedList<ChannelHandler>();
        this.outHandlers = new LinkedList<ChannelHandler>();

        this.consumerProtocol = consumerProtocol;
    }

    public final String getName()
    {
        return this.getClass().getSimpleName();
    }

    public List<ChannelHandler> getInHandlerList()
    {
        return this.inHandlers;
    }

    public List<ChannelHandler> getOutHandlerList()
    {
        return this.outHandlers;
    }

    public AbstractUSNProtocol getProtocol()
    {
        return this.consumerProtocol;
    }
}
