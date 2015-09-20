/**
 * @file TestClient.java
 * @brief Simple test client.
 */

package game.usn.bridge.test.e2e.testdata;

import java.util.Set;

import platform.bridge.api.observer.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.api.proxy.IClientProxyBase;
import platform.bridge.api.proxy.IResponseListener;
import platform.bridge.base.proxy.client.NettyClientProxy;
import platform.core.api.exception.BridgeException;

/**
 * Simple test client.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestClient implements IResponseListener
{
    // Base client proxy.
    IClientProxyBase clientProxyBase = null;

    // Bridge options
    private BridgeOptions bridgeOptions;

    // Channel observer set.
    private Set<IChannelObserver> channelObserverSet;

    // Testing client send.
    public boolean sent = false;
    public int sendCallbackCnt = 0;
    public boolean response;

    // Callbacks.
    private ITestTransportObserver testBridgeObserver;

    /**
     * Ctor.
     */
    public TestClient(BridgeOptions clientOptions, ITestTransportObserver testBridgeObserver,
        Set<IChannelObserver> channelObserverSet) throws BridgeException
    {
        bridgeOptions = clientOptions;
        this.testBridgeObserver = testBridgeObserver;
        this.channelObserverSet = channelObserverSet;

        clientProxyBase = new NettyClientProxy();
    }

    /**
     * Initialize test client on given port and host.
     * 
     * @param port
     * @param address
     * @throws BridgeException
     */
    public void initialize(int port, String address) throws BridgeException
    {
        clientProxyBase.initialize(address, port, this);
    }

    public void send() throws BridgeException
    {
        try
        {
            PingPacket packet = new PingPacket();
            clientProxyBase.sendPacket(packet);
            sendCallbackCnt++;
            sent = true;
            testBridgeObserver.clientSent(packet);
        }
        catch (Exception e)
        {}
    }

    @Override
    public void receive(AbstractPacket abstractPacket, String senderIdentifier)
    {
        if (abstractPacket instanceof PongPacket)
        {
            this.response = true;
            testBridgeObserver.clientReceived(abstractPacket);
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
