/**
 * @file ProxyTestBase.java
 * @brief Proxy test base functionality. Provides test data and utility methods for running platform proxy tests.
 */

package platform.bridge.proxy;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.ChannelOptions;
import platform.bridge.api.proxy.IClientProxyBase;
import platform.bridge.proxy.client.AbstractPlatformClientProxy;

/**
 * Proxy test base functionality. Provides test data and utility methods for running platform proxy tests.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ProxyTestBase
{
    /**
     * Client proxy for test purposes.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class TestClientProxy extends AbstractPlatformClientProxy
    {
        public ChannelOptions channelOptions;
        public String name;
        public AbstractPlatformProtocol protocol;
        public Set<IChannelObserver> channelObserverSet;
        public boolean channelUp;
        public boolean channelDown;
        public List<AbstractPacket> receivedPackets;

        public TestClientProxy(ChannelOptions channelOptions, String name, AbstractPlatformProtocol protocol,
            Set<IChannelObserver> channelObserverSet, IClientProxyBase clientProxyBase, int timeToBlock)
        {
            super(clientProxyBase, timeToBlock);

            this.channelOptions = channelOptions;
            this.name = name;
            this.protocol = protocol;
            this.channelObserverSet = channelObserverSet;

            receivedPackets = new ArrayList<AbstractPacket>();
        }

        @Override
        protected void receivePacket(AbstractPacket abstractPacket)
        {
            receivedPackets.add(abstractPacket);
        }

        @Override
        public ChannelOptions getChannelOptions()
        {
            return channelOptions;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public AbstractPlatformProtocol getProtocol()
        {
            return protocol;
        }

        @Override
        public Set<IChannelObserver> getChannelObserverSet()
        {
            return channelObserverSet;
        }

        @Override
        public void notifyChannelUp(String proxyName, InetSocketAddress address)
        {
            channelUp = true;
        }

        @Override
        public void notifyChannelDown(String proxyName)
        {
            channelDown = true;
        }
    }

}
