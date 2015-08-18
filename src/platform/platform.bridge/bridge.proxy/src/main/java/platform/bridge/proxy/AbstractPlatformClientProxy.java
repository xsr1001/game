/**
 * @file AbstractPlatformClientProxy.java
 * @brief <description>
 */

package platform.bridge.proxy;

import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import io.netty.channel.ChannelHandler;

import java.util.List;

import platform.service.api.IServiceProxy;

public abstract class AbstractPlatformClientProxy extends AbstractDataProxy implements IServiceProxy
{

    // Provided instance of protocol to use.
    private AbstractUSNProtocol consumerProtocol;

    // Proxy implementation specific in and out handler lists.
    private List<ChannelHandler> inHandlers;
    private List<ChannelHandler> outHandlers;

    // Provided channel options specific per proxy.
    private ChannelOptions channelOptions;

    protected AbstractPlatformClientProxy(AbstractUSNProtocol consumerProtocol)
    {
        super(consumerProtocol);
    }
}
