/**
 * @file ClientProxyTest.java
 * @brief <description>
 */

package platform.bridge.proxy.client;

import game.usn.bridge.USNBridgeManager;
import game.usn.bridge.api.listener.IChannelObserver;
import game.usn.bridge.pipeline.ChannelOptions;
import game.usn.bridge.proxy.AbstractBridgeAdapter;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import platform.bridge.proxy.ProxyTestBase;
import platform.core.api.exception.BridgeException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(USNBridgeManager.class)
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
        USNBridgeManager mockedBridgeManager = Mockito.mock(USNBridgeManager.class);
        Mockito.when(USNBridgeManager.getInstance()).thenReturn(mockedBridgeManager);

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
        Assert.assertTrue(ex instanceof BridgeException);
        ex = null;

        // Test OK.
        TestClientProxy testProxy1 = null;
        try
        {
            testProxy1 = new TestClientProxy(options, "testProxy1", new ProxyTestBase.TestProtocol1(), null, null,
                observerSet);

            Mockito.doAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation)
                {
                    Object[] args = invocation.getArguments();

                    bridgeAdapterResult = (AbstractBridgeAdapter) args[0];
                    observerSetResult = (Set<IChannelObserver>) args[1];
                    remoteHostIPv4Result = (String) args[2];
                    remoteHostPortResult = (Integer) args[3];
                    channelOptionsResult = (ChannelOptions) args[4];

                    return new Object();
                }
            }).when(mockedBridgeManager).registerClientProxy(testProxy1, observerSet, remoteHostPort, remoteHostIPv4,
                options);

            testProxy1.initialize(null, 1337);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        Assert.assertNull(remoteHostIPv4Result);
        Assert.assertEquals(observerSet, observerSetResult);
        Assert.assertEquals(options, channelOptionsResult);
        Assert.assertEquals(1337, remoteHostPortResult.intValue());
        Assert.assertEquals(testProxy1, bridgeAdapterResult);
    }
}
