/**
 * @file ClientProxyTest.java
 * @brief Bridge client proxy tests.
 */

package platform.bridge.proxy.client;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.proxy.ChannelOptions;
import platform.bridge.api.proxy.IClientProxyBase;
import platform.bridge.proxy.ProxyTestBase;

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
        // Prepare test data.
        Set<IChannelObserver> observerSet = new HashSet<IChannelObserver>();
        ChannelOptions options = new ChannelOptions(false, 10, false, null);
        int remoteHostPort = 1337;
        String remoteHostIPv4 = "remoteHostIPv4";
        String testProxyName = "testProxy1";

        TestClientProxy testProxy1 = null;

        // Mock client proxy base.
        IClientProxyBase clientProxyBase = Mockito.mock(IClientProxyBase.class);

        // Test invalid remote host ip.
        try
        {
            testProxy1 = new TestClientProxy(options, "testProxy1", new ProxyTestBase.TestProtocol1(), observerSet,
                clientProxyBase);
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
            testProxy1 = new TestClientProxy(options, "testProxy1", new ProxyTestBase.TestProtocol1(), observerSet,
                clientProxyBase);
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
            testProxy1 = new TestClientProxy(options, testProxyName, new ProxyTestBase.TestProtocol1(), observerSet,
                clientProxyBase);

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
}
