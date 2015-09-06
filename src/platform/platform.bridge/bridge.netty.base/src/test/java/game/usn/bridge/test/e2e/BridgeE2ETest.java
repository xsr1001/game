/**
 * @file BridgeServiceE2ETest.java
 * @brief End to end test for client and service proxy.
 */

package game.usn.bridge.test.e2e;

import game.usn.bridge.test.e2e.data.TestClient;
import game.usn.bridge.test.e2e.data.TestService;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import platform.bridge.api.listener.IConnectionObserver;
import platform.bridge.api.proxy.BridgeOptions;

/**
 * End to end test for client and service proxy.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class BridgeE2ETest
{
    // Exception.
    private Exception ex;

    // Server and client options.
    private static BridgeOptions serverOptions;
    private static BridgeOptions clientOptions;

    private CountDownLatch connectCDLatch;
    private CountDownLatch disconnectCDLatch;
    private CountDownLatch bindCDLatch;
    private CountDownLatch unbindCDLatch;

    /**
     * Initialize stuff before test.
     */
    @BeforeClass
    public static void beforeClass()
    {
        Set<IConnectionObserver> connSet = new HashSet<IConnectionObserver>();

        serverOptions = new BridgeOptions();
        serverOptions.set(BridgeOptions.KEY_IS_SERVER, Boolean.TRUE);
        serverOptions.set(BridgeOptions.KEY_READ_TIMEOUT_SEC, 5);
        serverOptions.set(BridgeOptions.KEY_CONNECTION_LISTENER_SET, connSet);

        clientOptions = new BridgeOptions();
        clientOptions.set(BridgeOptions.KEY_IS_SERVER, Boolean.FALSE);
    }

    /**
     * Reset/clean up before each test.
     */
    @Before
    public void before()
    {
        ex = null;

        connectCDLatch = new CountDownLatch(1);
        disconnectCDLatch = new CountDownLatch(1);
        bindCDLatch = new CountDownLatch(1);
        unbindCDLatch = new CountDownLatch(1);
    }

    /**
     * Test end to end data transmission.
     */
    @Test
    public void testE2E()
    {
        // Initialize service proxy.
        TestService testService = null;
        try
        {
            testService = new TestService(serverOptions, this);
            testService.init(0);
            Assert.assertTrue(bindCDLatch.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testService.observableCallbackCnt == 1);
            Assert.assertTrue(testService.bound);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);

        // Initialize client proxy.
        TestClient testClient = null;
        try
        {
            InetSocketAddress address = new InetSocketAddress(Inet4Address.getLocalHost(), 0);

            testClient = new TestClient(testService.servicePort, address.getHostName(), clientOptions, this);
            Assert.assertTrue(connectCDLatch.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testClient.observableCallbackCnt == 1);
            Assert.assertTrue(testClient.connected);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);

        // try
        // {
        // // Client send.
        // System.out.println("Sleeping for client send.");
        // Thread.sleep(1500);
        // Assert.assertTrue(this.testClient.sendCallbackCnt != 0);
        // Assert.assertTrue(this.testClient.sent);
        //
        // // Server receive.
        // System.out.println("Sleeping for server receive.");
        // Thread.sleep(1500);
        // Assert.assertTrue(this.testservice.received != false);
        // Assert.assertTrue(this.testservice.received);
        //
        // // Server send.
        // System.out.println("Sleeping for server send.");
        // Thread.sleep(1500);
        // Assert.assertTrue(this.testservice.sendCallbackCnt != 0);
        // Assert.assertTrue(this.testservice.sent);
        //
        // // Client receive.
        // System.out.println("Sleeping for client receive.");
        // Thread.sleep(1500);
        // Assert.assertTrue(this.testClient.response != false);
        // Assert.assertTrue(this.testClient.response);
        // }
        // catch (Exception e)
        // {
        // System.err.println(e);
        // ex = e;
        // }
        // Assert.assertNull(ex);
    }

    public void clientConnect()
    {
        connectCDLatch.countDown();
    }

    public void clientDisconnect()
    {
        disconnectCDLatch.countDown();
    }

    public void serverBind()
    {
        bindCDLatch.countDown();
    }

    public void serverUnbind()
    {
        unbindCDLatch.countDown();
    }
}
