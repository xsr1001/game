/**
 * @file ProxyTestBase.java
 * @brief Proxy test base functionality. Provides test data and utility methods for running platform proxy tests.
 */

package platform.bridge.proxy;

import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import platform.bridge.api.observer.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;
import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.api.proxy.IClientProxyBase;
import platform.bridge.api.proxy.transport.ITransportIdentifiable;
import platform.bridge.proxy.client.AbstractPlatformClientProxy;

/**
 * Proxy test base functionality. Provides test data and utility methods for running platform proxy tests.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ProxyTestBase
{
    // Test objects.
    protected static TestClientProxy testAsynchronousClientProxy = null;

    // Request queue.
    protected static Queue<AbstractPacket> requestQueue = new LinkedList<AbstractPacket>();

    // Asynchronous responder.
    private static Thread respoder;

    // Test data.
    protected int remoteHostPort = 1337;
    protected String remoteHostIPv4 = "remoteHostIPv4";
    protected Set<IChannelObserver> observerSet = new HashSet<IChannelObserver>();
    protected static BridgeOptions options = new BridgeOptions();
    protected String testProxyName = "testProxy1";
    protected String testProxyName2 = "testProxy2";

    // Public test data.
    public static final PacketP1 PACKET1_ID = new PacketP1();
    public static final PacketP2 PACKET2_ID = new PacketP2();
    public static final AbstractPacket PACKET3 = new PacketP3();
    public static final AbstractPacket PACKET4 = new PacketP4();
    public static final AbstractPlatformProtocol PROT1 = new TestProtocolP1();
    public static final AbstractPlatformProtocol PROT2 = new TestProtocolP2();

    @BeforeClass
    public static void beforeClass()
    {
        respoder = new Thread(asyncResponder);
        respoder.start();

        options.set(BridgeOptions.KEY_IS_SERVER, Boolean.FALSE);
    }

    @AfterClass
    public static void afterClass()
    {
        respoder.interrupt();
    }

    @Before
    public void before()
    {
        requestQueue.clear();
        testAsynchronousClientProxy = null;
    }

    /**
     * Client proxy for test purposes.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class TestClientProxy extends AbstractPlatformClientProxy implements IChannelObserver
    {
        public BridgeOptions channelOptions;
        public String name;
        public AbstractPlatformProtocol protocol;
        public Set<IChannelObserver> channelObserverSet;
        public boolean channelUp;
        public boolean channelDown;
        public List<AbstractPacket> receivedPackets;

        public TestClientProxy(BridgeOptions channelOptions, String name, AbstractPlatformProtocol protocol,
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
        public BridgeOptions getBridgeOptions()
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
        public void notifyChannelStateChanged(boolean isChannelUp, String proxyName, InetSocketAddress inetSocketAddress)
        {
            if (isChannelUp)
            {
                channelUp = true;
            }
            else
            {
                channelDown = true;
            }
        }
    }

    public static class PacketP1 extends AbstractPacket implements ITransportIdentifiable
    {
        private String testString = "test1";
        private UUID uuid;

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
        }

        @Override
        public void setTransportId(UUID id)
        {
            uuid = id;

        }

        @Override
        public UUID getTransportId()
        {
            return uuid;
        }
    }

    public static class PacketP2 extends AbstractPacket implements ITransportIdentifiable
    {
        private String testString = "test2";
        private int bla = 2;
        private UUID uuid;

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
            bla = readInt(buf);
        }

        @Override
        public void setTransportId(UUID id)
        {
            uuid = id;

        }

        @Override
        public UUID getTransportId()
        {
            return uuid;
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
            writeInt(bla, buf);
        }
    }

    public static class PacketP3 extends AbstractPacket
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

    public static class PacketP4 extends AbstractPacket
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

    private static class TestProtocolP1 extends AbstractPlatformProtocol
    {
        public TestProtocolP1()
        {
            super();
            registerPacket(1, PACKET1_ID.getClass());
        }
    }

    public static class TestProtocolP2 extends AbstractPlatformProtocol
    {
        public TestProtocolP2()
        {
            super();
            registerPacket(1, PACKET1_ID.getClass());
            registerPacket(2, PACKET2_ID.getClass());
            registerPacket(3, PACKET3.getClass());
        }
    }

    // Asynchronous responder.
    private static Runnable asyncResponder = new Runnable() {
        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    if (Thread.interrupted())
                    {
                        break;
                    }

                    if (requestQueue.size() > 0)
                    {
                        AbstractPacket pack = requestQueue.poll();
                        testAsynchronousClientProxy.receive(pack, null);
                    }
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException ie)
            {
                Thread.currentThread().interrupt();
            }
        }
    };

}
