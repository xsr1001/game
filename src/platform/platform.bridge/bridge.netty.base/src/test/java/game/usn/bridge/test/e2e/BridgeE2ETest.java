/**
 * @file BridgeServiceE2ETest.java
 * @brief <description>
 */

package game.usn.bridge.test.e2e;

import game.usn.bridge.test.e2e.data.TestClient;
import game.usn.bridge.test.e2e.data.TestService;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.listener.IConnectionObserver;
import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.base.PlatformBridgeManager;

/**
 * End to end test for bridge for service proxy.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class BridgeE2ETest
{
    // Exception.
    private Exception ex;

    // USN service.
    private static BridgeOptions serverOptions;
    private TestService testservice;
    Set<IChannelObserver> listenerSet;

    // USN client.
    private static BridgeOptions clientOptions;
    private TestClient testClient;
    Set<IChannelObserver> listenerSetClient;

    @BeforeClass
    public static void beforeClass()
    {
        Set<IConnectionObserver> connSet = new HashSet<IConnectionObserver>();
        serverOptions = new BridgeOptions(false, 555, true, connSet);

        Set<IConnectionObserver> connSetClient = new HashSet<IConnectionObserver>();
        clientOptions = new BridgeOptions(false, 555, false, connSetClient);
    }

    @Before
    public void before()
    {
        this.testservice = new TestService();
        this.listenerSet = new HashSet<IChannelObserver>();
        this.listenerSet.add(this.testservice);

        this.testClient = new TestClient();
        this.listenerSetClient = new HashSet<IChannelObserver>();
        this.listenerSetClient.add(this.testClient);

        this.ex = null;
    }

    /**
     * Test end to end data transmission. I am guilty of horrible checks using sleep function.
     */
    @Test
    public void testE2E()
    {
        try
        {
            PlatformBridgeManager.getInstance().registerServiceProxy(this.testservice, this.listenerSet, 0, serverOptions);

            // Server bind.
            System.out.println("Sleeping for server bind.");
            Thread.sleep(1500);
            Assert.assertTrue(this.testservice.observableCallbackCnt != 0);
            Assert.assertTrue(this.testservice.channelUp);

        }
        catch (Exception e)
        {
            System.err.println(e);
            ex = e;
        }
        Assert.assertNull(ex);

        try
        {
            InetSocketAddress address = new InetSocketAddress(Inet4Address.getLocalHost(), 0);
            PlatformBridgeManager.getInstance().registerClientProxy(this.testClient, this.listenerSetClient,
                this.testservice.servicePort, address.getHostName(), clientOptions);

            System.out.println("Sleeping for client connect.");
            Thread.sleep(1500);
            Assert.assertTrue(this.testClient.observableCallbackCnt != 0);
            Assert.assertTrue(this.testClient.channelUp);

            // Client send.
            System.out.println("Sleeping for client send.");
            Thread.sleep(1500);
            Assert.assertTrue(this.testClient.sendCallbackCnt != 0);
            Assert.assertTrue(this.testClient.sent);

            // Server receive.
            System.out.println("Sleeping for server receive.");
            Thread.sleep(1500);
            Assert.assertTrue(this.testservice.received != false);
            Assert.assertTrue(this.testservice.received);

            // Server send.
            System.out.println("Sleeping for server send.");
            Thread.sleep(1500);
            Assert.assertTrue(this.testservice.sendCallbackCnt != 0);
            Assert.assertTrue(this.testservice.sent);

            // Client receive.
            System.out.println("Sleeping for client receive.");
            Thread.sleep(1500);
            Assert.assertTrue(this.testClient.response != false);
            Assert.assertTrue(this.testClient.response);
        }
        catch (Exception e)
        {
            System.err.println(e);
            ex = e;
        }
        Assert.assertNull(ex);
    }
}
