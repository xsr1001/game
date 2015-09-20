/**
 * @file TestService.java
 * @brief Simple test service.
 */

package game.usn.bridge.test.e2e.testdata;

import java.util.Set;

import platform.bridge.api.observer.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.api.proxy.IResponseListener;
import platform.bridge.api.proxy.IServiceProxyBase;
import platform.bridge.base.proxy.service.NettyServiceProxy;
import platform.core.api.exception.BridgeException;

/**
 * Simple test service.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestService implements IResponseListener
{
    // Base service proxy.
    private IServiceProxyBase serviceProxyBase = null;

    // Callbacks.
    private ITestTransportObserver testBridgeObserver;

    // Channel observer set.
    private Set<IChannelObserver> channelObserverSet;

    // Bridge options
    private BridgeOptions bridgeOptions;

    // Testing ping pong.
    public boolean received = false;
    public boolean sent = false;
    public int sendCallbackCnt = 0;

    /**
     * Ctor.
     */
    public TestService(BridgeOptions serverOptions, ITestTransportObserver testBridgeObserver,
        Set<IChannelObserver> channelObserverSet)
    {
        bridgeOptions = serverOptions;
        this.channelObserverSet = channelObserverSet;
        this.testBridgeObserver = testBridgeObserver;

        serviceProxyBase = new NettyServiceProxy();
    }

    /**
     * Initialize test service on given port.
     * 
     * @param port
     * @throws BridgeException
     */
    public void initialize(int port) throws BridgeException
    {
        serviceProxyBase.initialize(port, this);
    }

    @Override
    public void receive(AbstractPacket abstractPacket, String senderIdentifier)
    {
        if (abstractPacket instanceof PingPacket)
        {
            received = true;
            testBridgeObserver.serverReceived(abstractPacket, senderIdentifier);
            try
            {
                PongPacket pongPacket = new PongPacket();
                serviceProxyBase.sendPacket(pongPacket, senderIdentifier);
                sendCallbackCnt++;
                sent = true;
                testBridgeObserver.serverSent(pongPacket, senderIdentifier);
            }
            catch (Exception e)
            {}
        }
    }

    @Override
    public BridgeOptions getBridgeOptions()
    {
        return bridgeOptions;
    }

    @Override
    public String getName()
    {
        return getClass().getName();
    }

    @Override
    public AbstractPlatformProtocol getProtocol()
    {
        return new TestServiceProtocol();
    }

    @Override
    public Set<IChannelObserver> getChannelObserverSet()
    {
        return channelObserverSet;
    }
}
