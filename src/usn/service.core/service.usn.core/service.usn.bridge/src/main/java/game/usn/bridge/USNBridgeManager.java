/**
 * @file USNBridge.java
 * @brief <description>
 */

package game.usn.bridge;

import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.IUSNProtocol;
import game.usn.bridge.api.listener.IConnectionListener;
import game.usn.bridge.api.listener.IServerListener;
import game.usn.bridge.api.proxy.AbstractDataProxy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;

import java.util.Set;

public final class USNBridgeManager extends AbstractBridgeProvider
{
    // Singleton instance.
    private static final USNBridgeManager INSTANCE = new USNBridgeManager();

    /**
     * Singleton instance getter.
     * 
     * @return instance of {@link USNBridgeManager}.
     */
    public static USNBridgeManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private USNBridgeManager()
    {

    }

    protected synchronized void registerServiceProxy(IUSNProtocol serviceProtocol,
        Set<IConnectionListener> externalConnectionListenerSet, Set<IServerListener> serverListenerSet,
        AbstractDataProxy serviceProxy) throws BridgeException
    {
        registerServiceProxy(serviceProtocol, externalConnectionListenerSet, serverListenerSet, serviceProxy, 0);
    }

    protected synchronized void registerServiceProxy(IUSNProtocol serviceProtocol,
        Set<IConnectionListener> externalConnectionListenerSet, Set<IServerListener> serverListenerSet,
        AbstractDataProxy serviceProxy, short servicePort) throws BridgeException
    {

    }




    public void start()
    {
        new ServerBootstrap().channel(PipelineUtils.getServerChannel()).option(ChannelOption.SO_REUSEADDR, true)
        .childAttr(PipelineUtils.LISTENER, info).childHandler(PipelineUtils.SERVER_CHILD).group(eventLoops).localAddress(
            info.getHost()).bind().addListener(listener);

    }
}
