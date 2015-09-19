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

import platform.bridge.api.observer.IConnectionObserver;
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
    private CountDownLatch clientSend;
    private CountDownLatch clientReceive;
    private CountDownLatch serverSend;
    private CountDownLatch serverReceive;

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
        clientSend = new CountDownLatch(1);
        clientReceive = new CountDownLatch(1);
        serverSend = new CountDownLatch(1);
        serverReceive = new CountDownLatch(1);
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
            testService = new TestService(serverOptions, 0, this);
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

        try
        {
            testClient.send();
            Assert.assertTrue(clientSend.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testClient.sendCallbackCnt != 0);
            Assert.assertTrue(testClient.sent);

            Assert.assertTrue(serverReceive.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testService.received != false);
            Assert.assertTrue(testService.received);

            Assert.assertTrue(serverSend.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testService.sendCallbackCnt != 0);
            Assert.assertTrue(testService.sent);

            Assert.assertTrue(clientReceive.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testClient.response != false);
            Assert.assertTrue(testClient.response);
        }
        catch (Exception e)
        {
            System.err.println(e);
            ex = e;
        }
        Assert.assertNull(ex);
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

    public void clientSend()
    {
        clientSend.countDown();
    }

    public void clientReceive()
    {
        clientReceive.countDown();
    }

    public void serverSend()
    {
        serverSend.countDown();
    }

    public void serverReceive()
    {
        serverReceive.countDown();
    }
}
