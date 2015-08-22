/**
 * @file RequestFuture.java
 * @brief <description>
 */

package platform.bridge.proxy;

import game.usn.bridge.api.protocol.AbstractPacket;
import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import game.usn.bridge.pipeline.ChannelOptions;
import io.netty.channel.ChannelHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import platform.core.api.exception.BridgeException;

public class RequestFuture
{

    // Provided instance of protocol to use.
    private AbstractUSNProtocol consumerProtocol;

    // Proxy implementation specific in and out handler lists.
    private List<ChannelHandler> inHandlers;
    private List<ChannelHandler> outHandlers;

    // Provided channel options specific per proxy.
    private ChannelOptions channelOptions;

    public RequestFuture(UUID messageId)
    {

    }

    public AbstractPacket get() throws BridgeException
    {
        return null;
    }

    public AbstractPacket get(int timeout, TimeUnit timeoutUnit) throws BridgeException
    {
        return null;
    }

    public void response(AbstractPacket packet);
}
