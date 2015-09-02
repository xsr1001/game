/**
 * @file ClientProxyTest.java
 * @brief Bridge client proxy tests.
 */

package platform.bridge.proxy.client;

import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.proxy.BridgeOptions;
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

    // Test results.
    private String remoteHostIPv4Result;
    private Integer remoteHostPortResult;

    /**
     * Reset stuff before each test.
     */
    @Before
    public void before()
    {
        ex = null;
        remoteHostIPv4Result = null;
        remoteHostPortResult = null;
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
            testProxy1 = new TestClientProxy(options, "testProxy1", PROT1, observerSet, clientProxyBase, 5);
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
            testProxy1 = new TestClientProxy(options, "testProxy1", PROT1, observerSet, clientProxyBase, 5);
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
            testProxy1 = new TestClientProxy(options, testProxyName, PROT1, observerSet, clientProxyBase, 5);

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
        Assert.assertEquals(options, testProxy1.getBridgeOptions());
    }

    /**
     * Test synchronous request functionality.
     */
    @Test
    public void testSynchronousRequest() throws Exception
    {
        // Mock client proxy base.
        IClientProxyBase clientProxyBase = Mockito.mock(IClientProxyBase.class);

        // Mock initialize.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                return new Object();
            }
        }).when(clientProxyBase).initialize(Mockito.anyString(), Mockito.anyInt(), Mockito.any(TestClientProxy.class));

        // Mock synchronous packet with response.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                requestQueue.add((AbstractPacket) invocation.getArguments()[0]);
                return new Object();
            }
        }).when(clientProxyBase).sendPacket(PACKET1_ID);

        // Mock synchronous packet without response.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                return new Object();
            }
        }).when(clientProxyBase).sendPacket(PACKET2_ID);

        try
        {
            BridgeOptions bo = new BridgeOptions();
            bo.set(BridgeOptions.KEY_IS_SERVER, Boolean.FALSE);

            testAsynchronousClientProxy = new TestClientProxy(bo, testProxyName2, PROT1,
                new HashSet<IChannelObserver>(), clientProxyBase, 3);
            testAsynchronousClientProxy.initialize(remoteHostIPv4, remoteHostPort);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        Assert.assertNotNull(testAsynchronousClientProxy);
        try
        {
            long pre = System.currentTimeMillis();
            testAsynchronousClientProxy.send(PACKET1_ID);
            long post = System.currentTimeMillis();
            Assert.assertTrue(post - pre < 3 * 1000);
        }
        catch (Exception e)
        {
            ex = e;
        }

        Assert.assertNull(ex);
        ex = null;

        try
        {
            long pre = System.currentTimeMillis();
            testAsynchronousClientProxy.send(PACKET2_ID);
            long post = System.currentTimeMillis();
            Assert.assertTrue(post - pre > 3 * 1000);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof BridgeException);
        Assert.assertTrue("ex.getMessage().startsWith(\"Timeout received while waiting for a result\"",
            ex.getMessage().startsWith("Timeout received while waiting for a result"));
        ex = null;
    }

    /**
     * Test asynchronous request.
     * 
     * @throws Exception
     */
    @Test
    public void testAsynchronousRequest() throws Exception
    {
        // Mock client proxy base.
        IClientProxyBase clientProxyBase = Mockito.mock(IClientProxyBase.class);

        // Mock initialize.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                return new Object();
            }
        }).when(clientProxyBase).initialize(Mockito.anyString(), Mockito.anyInt(), Mockito.any(TestClientProxy.class));

        // Mock synchronous packet with response.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                requestQueue.add((AbstractPacket) invocation.getArguments()[0]);
                return new Object();
            }
        }).when(clientProxyBase).sendPacket(PACKET1_ID);

        // Mock synchronous packet with response.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception
            {
                requestQueue.add((AbstractPacket) invocation.getArguments()[0]);
                return new Object();
            }
        }).when(clientProxyBase).sendPacket(PACKET3);

        try
        {
            BridgeOptions bo = new BridgeOptions();
            bo.set(BridgeOptions.KEY_IS_SERVER, Boolean.FALSE);
            testAsynchronousClientProxy = new TestClientProxy(bo, testProxyName2, PROT2,
                new HashSet<IChannelObserver>(), clientProxyBase, 3);
            testAsynchronousClientProxy.initialize(remoteHostIPv4, remoteHostPort);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        Assert.assertNotNull(testAsynchronousClientProxy);
        try
        {
            long pre = System.currentTimeMillis();
            testAsynchronousClientProxy.notify(PACKET1_ID);
            long post = System.currentTimeMillis();
            Assert.assertTrue(post - pre < 3 * 1000);
            testAsynchronousClientProxy.receive(PACKET3);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }
}
