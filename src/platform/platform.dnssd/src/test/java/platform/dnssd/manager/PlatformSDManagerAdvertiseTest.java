/**
 * @file PlatformSDManagerAdvertiseTest.java
 * @brief  Platform service discovery manager advertise operation test.
 */

package platform.dnssd.manager;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import platform.dnssd.api.IPlatformSDContextManager;
import platform.dnssd.api.endpoint.ISDEntity;

public class PlatformSDManagerAdvertiseTest implements ServiceListener
{
    // Field for testing fail scenario.
    private static Exception ex;

    // JmDNS manager to advertise test services.
    private static JmDNS jmDNSManager;

    // Wait mechanism for asynchronous results.
    private CountDownLatch doneResolved = new CountDownLatch(1);
    private CountDownLatch doneRemoved = new CountDownLatch(1);

    // Result lists.
    private List<ServiceInfo> resolvedServiceResultList = new ArrayList<ServiceInfo>();
    private List<ServiceInfo> removedServiceResultList = new ArrayList<ServiceInfo>();

    // Test data.
    private static final IPlatformSDContextManager TEST_PLATFORM_CONTEXT_MANAGER = new IPlatformSDContextManager() {
        @Override
        public String getPlatformId()
        {
            return "test1";
        }

        @Override
        public String getDomain()
        {
            return "local";
        }
    };

    private static final ISDEntity TEST_ENTITY_TYPE1 = new ISDEntity() {

        @Override
        public String getServiceType()
        {
            return "type1";
        }
    };

    private static final ISDEntity TEST_ENTITY_TYPE2 = new ISDEntity() {

        @Override
        public String getServiceType()
        {
            return "type1";
        }
    };

    private static final ISDEntity TEST_ENTITY_TYPE_SIMILAR1 = new ISDEntity() {

        @Override
        public String getServiceType()
        {
            return "type1";
        }
    };

    private static final String TEST_SERVICE_NAME1 = "test_name1";
    private static final String TEST_SERVICE_NAME2 = "test_name2";
    private static final int TEST_PORT1 = 55312;
    private static final int TEST_PORT2 = 55313;
    private static final Map<String, String> TEST_SERVICE_CONTEXT1 = new HashMap<String, String>();
    private static final Map<String, String> TEST_SERVICE_CONTEXT2 = new HashMap<String, String>();
    static
    {
        TEST_SERVICE_CONTEXT1.put("key1", "value1");
        TEST_SERVICE_CONTEXT1.put("key2", "value2");

        TEST_SERVICE_CONTEXT2.put("key12", "value12");
        TEST_SERVICE_CONTEXT2.put("key22", "value22");
    }

    /**
     * Initialized stuff before running tests.
     */
    @BeforeClass
    public static void BeforeClass() throws Exception
    {
        // Advertise test services.
        jmDNSManager = JmDNS.create();
    }

    /**
     * Initialize platform manager before each test.
     */
    @Before
    public void before() throws Exception
    {
        PlatformSDManager.getInstance().init(TEST_PLATFORM_CONTEXT_MANAGER);
        ex = null;
    }

    /**
     * Shut down platform SD manager after each test and clean.
     * 
     * @throws Exception
     */
    @After
    public void after() throws Exception
    {
        PlatformSDManager.getInstance().shutdown();
        doneResolved = new CountDownLatch(1);
        doneRemoved = new CountDownLatch(1);

        resolvedServiceResultList.clear();
        removedServiceResultList.clear();
    }

    /**
     * Helper method for starting browse operation.
     * 
     * @param serviceType
     *            - a {@link String} service type to browse for.
     */
    private void doBrowse(String serviceType)
    {
        jmDNSManager.addServiceListener(
            "_".concat(serviceType).concat("._tcp.").concat(TEST_PLATFORM_CONTEXT_MANAGER.getPlatformId()).concat(".").concat(
                TEST_PLATFORM_CONTEXT_MANAGER.getDomain()).concat("."), this);
    }

    /**
     * Helper method for stopping browse operation.
     * 
     * @param serviceType
     *            - a {@link String} service type to stop browsing for.
     */
    private void stopBrowse(String serviceType)
    {
        jmDNSManager.removeServiceListener(
            "_".concat(serviceType).concat("._tcp.").concat(TEST_PLATFORM_CONTEXT_MANAGER.getPlatformId()).concat(".").concat(
                TEST_PLATFORM_CONTEXT_MANAGER.getDomain()).concat("."), this);
    }

    /**
     * Helper method for extracting service context map for service info.
     * 
     * @param serviceInfo
     *            - a {@link ServiceInfo} source resolved service date.
     * @return a {@link Map} of {@link String} service context keys to {@link String} service context values.
     */
    private Map<String, String> extractContextMap(ServiceInfo serviceInfo)
    {
        Map<String, String> serviceContextMap = new HashMap<String, String>();
        Enumeration<String> propertyNames = serviceInfo.getPropertyNames();
        while (propertyNames.hasMoreElements())
        {
            String propertyName = propertyNames.nextElement();
            serviceContextMap.put(propertyName, serviceInfo.getPropertyString(propertyName));
        }

        return serviceContextMap;
    }

    /**
     * Test using SD manager without initializing it.
     * 
     * @throws Exception
     */
    @Test
    public void testNotInitialized()
    {
        PlatformSDManager.getInstance().shutdown();

        try
        {
            PlatformSDManager.getInstance().advertise(null, null, 1, null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Sevice discovery manager has not been initialized yet."));
        ex = null;

        try
        {
            PlatformSDManager.getInstance().advertiseStop(null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Sevice discovery manager has not been initialized yet."));
        ex = null;
    }

    /**
     * Test advertise with invalid parameters.
     */
    @Test
    public void testAdvertiseInvalidParameters()
    {
        // Test invalid argument.
        try
        {
            PlatformSDManager.getInstance().advertise(null, null, 1, null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        try
        {
            PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE1, null, 1, null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        try
        {
            PlatformSDManager.getInstance().advertise(null, TEST_SERVICE_NAME1, 1, null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;
    }

    /**
     * Tests advertise OK.
     */
    @Test
    public void testAdvertiseOK() throws Exception
    {
        // Test advertise OK.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
            Assert.assertTrue(PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE1, TEST_SERVICE_NAME1,
                TEST_PORT1, TEST_SERVICE_CONTEXT1));
            Assert.assertTrue(PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        doBrowse(TEST_ENTITY_TYPE1.getServiceType());

        boolean doneWithin = false;
        if (doneResolved.getCount() == 1)
        {
            doneWithin = doneResolved.await(10, TimeUnit.SECONDS);
        }
        else
        {
            doneWithin = true;
        }

        stopBrowse(TEST_ENTITY_TYPE1.getServiceType());

        Assert.assertTrue(doneWithin);
        Assert.assertEquals(1, resolvedServiceResultList.size());
        Assert.assertNotNull(resolvedServiceResultList.get(0));

        Assert.assertEquals(TEST_ENTITY_TYPE1.getServiceType(), resolvedServiceResultList.get(0).getApplication());
        Assert.assertEquals(TEST_SERVICE_NAME1, resolvedServiceResultList.get(0).getName());

        Map<String, String> contextMap = extractContextMap(resolvedServiceResultList.get(0));
        Assert.assertNotNull(contextMap);
        Assert.assertEquals(2, contextMap.size());
        Assert.assertEquals(TEST_SERVICE_CONTEXT1, contextMap);

        // Test advertise fail due to multiple services from same entity.
        try
        {
            Assert.assertTrue(PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
            Assert.assertTrue(!PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE1, TEST_SERVICE_NAME2,
                TEST_PORT1, TEST_SERVICE_CONTEXT1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Test advertise fail due to advertise with same duplicate name.
        try
        {
            Assert.assertTrue(PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
            Assert.assertTrue(!PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE2, TEST_SERVICE_NAME1,
                TEST_PORT1, TEST_SERVICE_CONTEXT1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test proper check to equality of internal {@link ServiceInfo} objects.
     * 
     * @throws Exception
     */
    @Test
    public void testAdvertiseProperEqualityCheck() throws Exception
    {
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
            Assert.assertTrue(PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE1, TEST_SERVICE_NAME1,
                TEST_PORT1, TEST_SERVICE_CONTEXT1));
            Assert.assertTrue(PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        Assert.assertFalse(PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE_SIMILAR1, TEST_SERVICE_NAME1,
            TEST_PORT1, TEST_SERVICE_CONTEXT1));
        Assert.assertFalse(PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE_SIMILAR1, TEST_SERVICE_NAME1,
            TEST_PORT1, TEST_SERVICE_CONTEXT2));
        Assert.assertFalse(PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE_SIMILAR1, TEST_SERVICE_NAME1,
            TEST_PORT2, TEST_SERVICE_CONTEXT2));
    }

    /**
     * Test advertise stop with invalid parameters.
     */
    @Test
    public void testAdvertiseStopInvalidParams()
    {
        // Test invalid argument.
        try
        {
            PlatformSDManager.getInstance().advertiseStop(null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;
    }

    /**
     * Test advertise stop OK.
     */
    @Test
    public void testAdvertiseStopOK() throws Exception
    {
        // Test stop advertise, not yet advertised.
        try
        {
            PlatformSDManager.getInstance().advertiseStop(TEST_ENTITY_TYPE1);
            Assert.assertTrue(!PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Test advertise OK, wait loop should time out.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
            Assert.assertTrue(PlatformSDManager.getInstance().advertise(TEST_ENTITY_TYPE1, TEST_SERVICE_NAME1,
                TEST_PORT1, TEST_SERVICE_CONTEXT1));
            Assert.assertTrue(PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            PlatformSDManager.getInstance().advertiseStop(TEST_ENTITY_TYPE1);
            Assert.assertTrue(!PlatformSDManager.getInstance().isAdvertised(TEST_ENTITY_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Sometimes, JmDNS client caches results, dirty way to wait a bit.
        Thread.sleep(3000);

        // wait loop should time out.
        doBrowse(TEST_ENTITY_TYPE1.getServiceType());
        boolean doneWithin = false;
        if (doneResolved.getCount() == 1)
        {
            doneWithin = doneResolved.await(5, TimeUnit.SECONDS);
        }
        else
        {
            doneWithin = true;
        }
        stopBrowse(TEST_ENTITY_TYPE1.getServiceType());

        Assert.assertFalse(doneWithin);
        Assert.assertEquals(0, resolvedServiceResultList.size());
    }

    @Override
    public void serviceAdded(ServiceEvent event)
    {
        jmDNSManager.requestServiceInfo(event.getType(), event.getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent event)
    {
        removedServiceResultList.add(event.getInfo());
        doneRemoved.countDown();
    }

    @Override
    public void serviceResolved(ServiceEvent event)
    {
        resolvedServiceResultList.add(event.getInfo());
        doneResolved.countDown();
    }
}
