/**
 * @file ProxyTestBase.java
 * @brief Proxy test base functionality. Provides test data and utility methods for running platform proxy tests.
 */

package platform.bridge.proxy;

import io.netty.buffer.ByteBuf;

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
            Set<IChannelObserver> channelObserverSet, IClientProxyBase clientProxyBase)
        {
            super(clientProxyBase);

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

    /**
     * Test packet 1.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class Packet1 extends AbstractPacket
    {
        private String testString = "test1";

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
        }
    }

    /**
     * Test packet 2.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class Packet2 extends AbstractPacket
    {
        private String testString = "test2";
        private int bla = 2;

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
            bla = readInt(buf);
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
            writeInt(bla, buf);
        }
    }

    /**
     * Test protocol 1.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class TestProtocol1 extends AbstractPlatformProtocol
    {
        public TestProtocol1()
        {
            super();
            registerPacket(1, Packet1.class);
        }
    }

    /**
     * Test protocol 2.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class TestProtocol2 extends AbstractPlatformProtocol
    {
        public TestProtocol2()
        {
            super();
            registerPacket(1, Packet1.class);
            registerPacket(2, Packet2.class);
        }
    }
}
