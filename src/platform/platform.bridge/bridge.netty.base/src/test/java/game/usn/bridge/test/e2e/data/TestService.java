/**
 * @file TestService.java
 * @brief Simple test service.
 */

package game.usn.bridge.test.e2e.data;

import game.usn.bridge.test.e2e.BridgeE2ETest;

import java.net.InetSocketAddress;
import java.util.Set;

import platform.bridge.api.listener.IChannelObserver;
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
    IServiceProxyBase serviceProxyBase = null;

    // Testing channel bound.
    public boolean bound = false;
    public int observableCallbackCnt = 0;
    public int servicePort = -1;

    // Testing ping pong.
    public boolean received = false;
    public boolean sent = false;
    public int sendCallbackCnt = 0;

    // Bridge options
    private BridgeOptions bridgeOptions;

    private BridgeE2ETest callback;

    /**
     * Ctor.
     */
    public TestService(BridgeOptions serverOptions, int port, BridgeE2ETest callback) throws BridgeException
    {
        bridgeOptions = serverOptions;
        serviceProxyBase = new NettyServiceProxy();
        serviceProxyBase.initialize(port, this);
        this.callback = callback;
    }

    @Override
    public void notifyChannelUp(String proxyName, InetSocketAddress address)
    {
        bound = true;
        observableCallbackCnt++;
        servicePort = address.getPort();
        callback.serverBind();
    }

    @Override
    public void notifyChannelDown(String proxyName)
    {
        bound = false;
        observableCallbackCnt++;
        callback.serverUnbind();

    }

    @Override
    public void receive(AbstractPacket abstractPacket, String senderIdentifier)
    {
        if (abstractPacket instanceof PingPacket)
        {
            received = true;
            callback.serverReceive();
            try
            {
                serviceProxyBase.sendPacket(new PongPacket(), senderIdentifier);
                sendCallbackCnt++;
                sent = true;
                callback.serverSend();
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
        return null;
    }
}
