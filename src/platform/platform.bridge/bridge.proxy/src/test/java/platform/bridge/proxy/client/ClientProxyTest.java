/**
 * @file ClientProxyTest.java
 * @brief Bridge client proxy tests.
 */

package platform.bridge.proxy.client;

import game.usn.bridge.api.test.protocol.TestAbstractPacket;
import game.usn.bridge.api.test.protocol.TestPlatformProtocol;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.proxy.ChannelOptions;
import platform.bridge.api.proxy.IClientProxyBase;
import platform.bridge.proxy.ProxyTestBase;
import platform.core.api.exception.BridgeException;

/**
 * Bridge client proxy tests.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ClientProxyTest extends ProxyTestBase
{
    // Test checks.
    private Exception ex = null;

    // Test data.
    private Set<IChannelObserver> observerSet = new HashSet<IChannelObserver>();
    private ChannelOptions options = new ChannelOptions(false, 10, false, null);
    private int remoteHostPort = 1337;
    private String remoteHostIPv4 = "remoteHostIPv4";
    private String testProxyName = "testProxy1";
    private String testProxyName2 = "testProxy2";

    // Test results.
    private String remoteHostIPv4Result;
    private Integer remoteHostPortResult;

    // Test objects.
    TestClientProxy testProxy2 = null;

    /**
     * Reset stuff before each test.
     */
    @Before
    public void before()
    {
        ex = null;
        remoteHostIPv4Result = null;
        remoteHostPortResult = null;

        testProxy2 = null;
    }

    /**
     * Test client proxy.
     */
    @Test
    public void testClientProxy() throws Exception
    {
        TestClientProxy testProxy1 = null;

        // Mock client proxy base.
        IClientProxyBase clientProxyBase = Mockito.mock(IClientProxyBase.class);

        // Test invalid remote host ip.
        try
        {
            testProxy1 = new TestClientProxy(options, "testProxy1", TestPlatformProtocol.PROT1, observerSet,
                clientProxyBase, 5);
            testProxy1.initialize(null, remoteHostPort);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IllegalArgumentException);
        ex = null;

        // Test invalid remove host port.
        try
        {
            testProxy1 = new TestClientProxy(options, "testProxy1", TestPlatformProtocol.PROT1, observerSet,
                clientProxyBase, 5);
            testProxy1.initialize(remoteHostIPv4, null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IllegalArgumentException);
        ex = null;

        // Test OK.
        try
        {
            testProxy1 = new TestClientProxy(options, testProxyName, TestPlatformProtocol.PROT1, observerSet,
                clientProxyBase, 5);

            Mockito.doAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Exception
                {
                    remoteHostIPv4Result = (String) invocation.getArguments()[0];
                    remoteHostPortResult = (Integer) invocation.getArguments()[1];

                    return new Object();
                }
            }).when(clientProxyBase).initialize(remoteHostIPv4, remoteHostPort, testProxy1);

            testProxy1.initialize(remoteHostIPv4, remoteHostPort);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        Assert.assertEquals(remoteHostIPv4, remoteHostIPv4Result);
        Assert.assertEquals(remoteHostPort, remoteHostPortResult.intValue());
        Assert.assertEquals(testProxyName, testProxy1.getName());
        Assert.assertEquals(options, testProxy1.getChannelOptions());
    }

    /**
     * Test synchronous request functionality.
     */
    @Test
    public void testSynchronousRequest() throws Exception
    {
        // Mock client proxy base.
        IClientProxyBase clientProxyBase = Mockito.mock(IClientProxyBase.class);

        // Request queue.
        Queue<AbstractPacket> requestQueue = new LinkedList<AbstractPacket>();

        // Mock initialize.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                remoteHostIPv4Result = (String) invocation.getArguments()[0];
                remoteHostPortResult = (Integer) invocation.getArguments()[1];

                return new Object();
            }
        }).when(clientProxyBase).initialize(remoteHostIPv4, remoteHostPort, testProxy2);

        // Mock synchronous packet with response.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                requestQueue.add((AbstractPacket) invocation.getArguments()[0]);
                return new Object();
            }
        }).when(clientProxyBase).sendPacket(TestAbstractPacket.PACKET1_ID);

        // Mock synchronous packet without response.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                return new Object();
            }
        }).when(clientProxyBase).sendPacket(TestAbstractPacket.PACKET2_ID);

        // Fetch requests from proxy and echo back - simulating async response.
        Runnable run = new Runnable() {
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
                            testProxy2.receive(pack);
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
        Thread checker = new Thread(run);
        checker.start();

        try
        {
            testProxy2 = new TestClientProxy(new ChannelOptions(false, 10, false, null), testProxyName2,
                TestPlatformProtocol.PROT1, new HashSet<IChannelObserver>(), clientProxyBase, 3);

            testProxy2.initialize(remoteHostIPv4, remoteHostPort);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        Assert.assertNotNull(testProxy2);
        try
        {
            long pre = System.currentTimeMillis();
            testProxy2.send(TestAbstractPacket.PACKET1_ID);
            long post = System.currentTimeMillis();
            Assert.assertTrue(post - pre < 3 * 1000);
        }
        catch (Exception e)
        {
            ex = e;
        }
        finally
        {
            checker.interrupt();
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            long pre = System.currentTimeMillis();
            testProxy2.send(TestAbstractPacket.PACKET2_ID);
            long post = System.currentTimeMillis();
            Assert.assertTrue(post - pre > 3 * 1000);
        }
        catch (Exception e)
        {
            ex = e;
        }
        finally
        {
            checker.interrupt();
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof BridgeException);
        Assert.assertTrue("ex.getMessage().startsWith(\"Timeout received while waiting for a result\"",
            ex.getMessage().startsWith("Timeout received while waiting for a result"));
        ex = null;
    }

    @Test
    public void testAsynchronousRequest()
    {

    }
}
