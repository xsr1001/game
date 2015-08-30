/**
 * @file ClientProxyTest.java
 * @brief <description>
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
import platform.bridge.base.proxy.AbstractBridgeAdapter;
import platform.bridge.proxy.ProxyTestBase;

public class ClientProxyTest extends ProxyTestBase
{
    // Test checks.
    private Exception ex = null;

    private AbstractBridgeAdapter bridgeAdapterResult;
    private Set<IChannelObserver> observerSetResult;
    private String remoteHostIPv4Result;
    private Integer remoteHostPortResult;
    private ChannelOptions channelOptionsResult;

    /**
     * Reset stuff before each test.
     */
    @Before
    public void before()
    {
        ex = null;
        bridgeAdapterResult = null;
        observerSetResult = null;
        remoteHostIPv4Result = null;
        remoteHostPortResult = null;
        channelOptionsResult = null;
    }

    /**
     * Test client proxy.
     */
    @Test
    public void testClientProxy()
    {
        Set<IChannelObserver> observerSet = new HashSet<IChannelObserver>();
        ChannelOptions options = new ChannelOptions(false, 10, false, null);
        int remoteHostPort = 1337;
        String remoteHostIPv4 = "remoteHostIPv4";

        // Test invalid channel options.
        try
        {
            TestClientProxy testProxy1 = new TestClientProxy(null, "testProxy1", new ProxyTestBase.TestProtocol1(),
                null, null, observerSet);
            testProxy1.initialize(remoteHostIPv4, 1337);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IllegalArgumentException);
        ex = null;

        // Test invalid remote host ip.
        try
        {
            TestClientProxy testProxy1 = new TestClientProxy(options, "testProxy1", new ProxyTestBase.TestProtocol1(),
                null, null, observerSet);
            testProxy1.initialize(null, 1337);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IllegalArgumentException);
        ex = null;

        // Test OK.
        TestClientProxy testProxy1 = null;
        try
        {
            testProxy1 = Mockito.mock(TestClientProxy.class);
            testProxy1.name = "testProxy1";
            testProxy1.channelOptions = options;
            testProxy1.protocol = new ProxyTestBase.TestProtocol1();
            testProxy1.channelObserverSet = observerSet;

            Mockito.doAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation)
                {
                    return new Object();
                }
            }).when(testProxy1).initialize(remoteHostIPv4, remoteHostPort);

            testProxy1.initialize(null, 1337);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        Assert.assertEquals(observerSet, testProxy1.channelObserverSet);
        Assert.assertEquals(options, testProxy1.channelOptions);
    }
}
