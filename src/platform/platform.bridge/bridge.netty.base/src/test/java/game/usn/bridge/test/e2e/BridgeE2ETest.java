/**
 * @file BridgeServiceE2ETest.java
 * @brief End to end test for client and service proxy.
 */

package game.usn.bridge.test.e2e;

import game.usn.bridge.test.e2e.testdata.ITestTransportObserver;
import game.usn.bridge.test.e2e.testdata.TestClient;
import game.usn.bridge.test.e2e.testdata.TestService;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import platform.bridge.api.observer.IChannelObserver;
import platform.bridge.api.observer.IConnectionObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.proxy.BridgeOptions;

/**
 * End to end test for client and service proxy.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class BridgeE2ETest implements ITestTransportObserver, IChannelObserver
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

    // Test service and client.
    private TestService testService1;
    private TestClient testClient1;

    // Testing service channel bound.
    private boolean bound = false;
    private int observableCallbackCnt = 0;
    private int servicePort = -1;

    // Testing client channel bound.
    private int clientObservableCallbackCnt = 0;
    private boolean connected;

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

        testService1 = null;
        testClient1 = null;

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
        try
        {
            testService1 = new TestService(serverOptions, this, new HashSet<IChannelObserver>(
                Arrays.asList(new IChannelObserver[] { this })));
            testService1.initialize(0);

            Assert.assertTrue(bindCDLatch.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(observableCallbackCnt == 1);
            Assert.assertTrue(bound);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);

        // Initialize client proxy.
        try
        {
            InetSocketAddress address = new InetSocketAddress(Inet4Address.getLocalHost(), 0);

            testClient1 = new TestClient(clientOptions, this, new HashSet<IChannelObserver>(
                Arrays.asList(new IChannelObserver[] { this })));
            testClient1.initialize(servicePort, address.getHostName());
            Assert.assertTrue(connectCDLatch.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(clientObservableCallbackCnt == 1);
            Assert.assertTrue(connected);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);

        try
        {
            testClient1.send();
            Assert.assertTrue(clientSend.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testClient1.sendCallbackCnt != 0);
            Assert.assertTrue(testClient1.sent);

            Assert.assertTrue(serverReceive.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testService1.received != false);
            Assert.assertTrue(testService1.received);

            Assert.assertTrue(serverSend.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testService1.sendCallbackCnt != 0);
            Assert.assertTrue(testService1.sent);

            Assert.assertTrue(clientReceive.await(2, TimeUnit.SECONDS));
            Assert.assertTrue(testClient1.response != false);
            Assert.assertTrue(testClient1.response);
        }
        catch (Exception e)
        {
            System.err.println(e);
            ex = e;
        }
        Assert.assertNull(ex);
    }

    @Override
    public void notifyChannelStateChanged(boolean isChannelUp, String proxyName, InetSocketAddress inetSocketAddress)
    {
        if (proxyName.compareTo(testService1.getName()) == 0)
        {
            if (isChannelUp)
            {
                bound = true;
                observableCallbackCnt++;
                bindCDLatch.countDown();
                servicePort = inetSocketAddress.getPort();
            }
            else
            {
                bound = false;
                observableCallbackCnt++;
                unbindCDLatch.countDown();
            }
        }
        else if (proxyName.compareTo(testClient1.getName()) == 0)
        {
            if (isChannelUp)
            {
                connected = true;
                clientObservableCallbackCnt++;
                connectCDLatch.countDown();
            }
            else
            {
                connected = false;
                clientObservableCallbackCnt++;
                disconnectCDLatch.countDown();
            }
        }
    }

    @Override
    public void clientSent(AbstractPacket abstractPacket)
    {
        clientSend.countDown();
    }

    @Override
    public void clientReceived(AbstractPacket abstractPacket)
    {
        clientReceive.countDown();
    }

    @Override
    public void serverSent(AbstractPacket abstractPacket, String senderIdentifier)
    {
        serverSend.countDown();
    }

    @Override
    public void serverReceived(AbstractPacket abstractPacket, String senderIdentifier)
    {
        serverReceive.countDown();
    }
}
