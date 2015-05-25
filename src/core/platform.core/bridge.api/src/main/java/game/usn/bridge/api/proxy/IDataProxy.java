/**
 * @file IDataProxy.java
 * @brief <description>
 */

package game.usn.bridge.api.proxy;

import game.usn.bridge.api.IUSNProtocol;
import io.netty.channel.ChannelHandler;

import java.util.List;

public interface IDataProxy
{
    String getName();

    List<ChannelHandler> getInHandlerList();

    List<ChannelHandler> getOutHandlerList();

    IUSNProtocol getProtocol();
}
