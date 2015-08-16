/**
 * @file ServiceProxyRegisterTest.java
 * @brief Test for registering and retrieving service proxies.
 */

package platform.service.infrastructure.proxy.test;

import java.net.InetAddress;

import junit.framework.Assert;

import org.junit.Test;

import platform.core.api.exception.BridgeException;
import platform.service.api.IServiceProxy;
import platform.service.infrastructure.proxy.ServiceProxyRegister;

/**
 * Test for registering and retrieving service proxies.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ServiceProxyRegisterTest
{
    // Test service proxy for failed instantiation (no constructor).
    private IServiceProxy failInstantiateServiceProxy = new IServiceProxy() {

        @Override
        public void initialize(InetAddress serviceAddress) throws BridgeException
        {

        }

        @Override
        public void release() throws BridgeException
        {

        }
    };

    /**
     * Test register proxy.
     */
    @Test
    public void testRegisterProxy()
    {
        Exception ex = null;

        // Test invalid argument.
        try
        {
            ServiceProxyRegister.getInstance().registerProxy("test type", null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Illegal argument provided.\")",
            ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        // Test invalid argument.
        try
        {
            ServiceProxyRegister.getInstance().registerProxy(null, OKServiceProxy.class);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Illegal argument provided.\")",
            ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        // Test ok.
        try
        {
            ServiceProxyRegister.getInstance().registerProxy("serviceType1", FailServiceProxy.class);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Test duplicate fail.

        // Test invalid argument.
        try
        {
            ServiceProxyRegister.getInstance().registerProxy("serviceType1", FailServiceProxy.class);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Service proxy already registered for service type\")",
            ex.getLocalizedMessage().startsWith("Service proxy already registered for service type"));
        ex = null;
    }

    // Test get proxy.
    @Test
    public void testGetProxy()
    {
        Exception ex = null;

        // Test invalid argument.
        try
        {
            ServiceProxyRegister.getInstance().getServiceProxy("test type", null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Illegal argument provided.\")",
            ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        // Test invalid argument.
        try
        {
            ServiceProxyRegister.getInstance().getServiceProxy(null, InetAddress.getLoopbackAddress());
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Illegal argument provided.\")",
            ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        // Test fail proxy not registered.
        try
        {
            ServiceProxyRegister.getInstance().getServiceProxy("serviceType2", InetAddress.getLoopbackAddress());
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Service proxy for service type\")",
            ex.getLocalizedMessage().startsWith("Service proxy for service type"));
        ex = null;

        // Test fail proxy initialization.
        try
        {
            ServiceProxyRegister.getInstance().registerProxy("serviceType3", FailServiceProxy.class);
            ServiceProxyRegister.getInstance().getServiceProxy("serviceType3", InetAddress.getLoopbackAddress());
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Error initializing service proxy.\")",
            ex.getLocalizedMessage().startsWith("Error initializing service proxy."));
        ex = null;

        // Test fail proxy instantiation.
        try
        {
            ServiceProxyRegister.getInstance().registerProxy("serviceType5", failInstantiateServiceProxy.getClass());
            ServiceProxyRegister.getInstance().getServiceProxy("serviceType5", InetAddress.getLoopbackAddress());
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue("ex.getLocalizedMessage().startsWith(\"Service proxy cannot be instantiated\")",
            ex.getLocalizedMessage().startsWith("Service proxy cannot be instantiated"));
        ex = null;

        // Test OK
        try
        {
            ServiceProxyRegister.getInstance().registerProxy("serviceType4", OKServiceProxy.class);
            ServiceProxyRegister.getInstance().getServiceProxy("serviceType4", InetAddress.getLoopbackAddress());
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
    }
}
