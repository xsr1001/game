/**
 * @file TestClient.java
 * @brief Simple test client.
 */

package game.usn.bridge.test.e2e.data;

import game.usn.bridge.test.e2e.BridgeE2ETest;

import java.net.InetSocketAddress;
import java.util.Set;

import platform.bridge.api.listener.IChannelObserver;
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

    // Testing channel up (client has connected).
    public boolean connected = false;
    public int observableCallbackCnt = 0;

    // Testing client send.
    public boolean sent = false;
    public int sendCallbackCnt = 0;
    public boolean response;

    // Callbacks.
    private BridgeE2ETest callback;

    /**
     * Ctor.
     */
    public TestClient(int port, String address, BridgeOptions clientOptions, BridgeE2ETest callback)
        throws BridgeException
    {
        bridgeOptions = clientOptions;

        clientProxyBase = new NettyClientProxy();
        clientProxyBase.initialize(address, port, this);

        this.callback = callback;
    }

    @Override
    public void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        connected = true;
        observableCallbackCnt++;
        callback.clientConnect();
    }

    public void send() throws BridgeException
    {
        try
        {
            clientProxyBase.sendPacket(new PingPacket());
            sendCallbackCnt++;
            sent = true;
            callback.clientSend();
        }
        catch (Exception e)
        {}
    }

    @Override
    public void notifyChannelDown(String proxyName)
    {
        connected = false;
        observableCallbackCnt++;
        callback.clientDisconnect();
    }

    @Override
    public void receive(AbstractPacket abstractPacket, String senderIdentifier)
    {
        if (abstractPacket instanceof PongPacket)
        {
            this.response = true;
            callback.clientReceive();
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
        return null;
    }
}
