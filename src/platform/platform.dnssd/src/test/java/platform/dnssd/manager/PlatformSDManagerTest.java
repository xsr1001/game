/**
 * @file PlatformSDManagerTest.java
 * @brief <description>
 */

package platform.dnssd.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import platform.dnssd.api.IPlatformSDContextManager;
import platform.dnssd.api.filter.SDEntityBrowseResult;
import platform.dnssd.api.listener.ISDListener;

public class PlatformSDManagerTest implements ISDListener
{
    // Test SD context manager.
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

    // Test data.
    private static final String TEST_SERVICE_TYPE1 = "type1";
    private static final String TEST_SERVICE_NAME1 = "test_name1";
    private static final int TEST_SERVICE_PORT1 = 55362;
    private static Map<String, String> propertyMap = new HashMap<String, String>();
    static
    {
        propertyMap.put("key1", "value1");
        propertyMap.put("key2", "value2");
    }
    private static final ServiceInfo SERVICE_INFO1 = ServiceInfo.create(
        "_".concat(TEST_SERVICE_TYPE1).concat("._tcp.").concat(TEST_PLATFORM_CONTEXT_MANAGER.getPlatformId()).concat(
            ".").concat(TEST_PLATFORM_CONTEXT_MANAGER.getDomain()).concat("."), TEST_SERVICE_NAME1, TEST_SERVICE_PORT1,
        0, 0, true, propertyMap);

    // JmDNS manager to advertise test services.
    private static JmDNS jmDNSManager;

    // Wait mechanism for asynchronous results.
    final CountDownLatch done = new CountDownLatch(1);

    // Result list.
    List<SDEntityBrowseResult> resultList;

    /**
     * Test using SD manager without initializing it before running other tests. Initialized stuff before running tests.
     */
    @BeforeClass
    public static void testNotInitializedAndBeforeClass() throws Exception
    {
        Exception ex = null;
        try
        {
            PlatformSDManager.getInstance().advertise(null, null, 0, null);
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

        try
        {
            PlatformSDManager.getInstance().browse(null, null, null);
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
            PlatformSDManager.getInstance().browseStop(null, null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Sevice discovery manager has not been initialized yet."));
        ex = null;

        // Initialize platform SD manager.
        PlatformSDManager.getInstance().init(TEST_PLATFORM_CONTEXT_MANAGER);

        // Advertise test services.
        jmDNSManager = JmDNS.create();
        jmDNSManager.registerService(SERVICE_INFO1);
    }

    /**
     * Cleanup in the end.
     */
    @AfterClass
    public static void afterClass()
    {
        jmDNSManager.unregisterAllServices();
    }

    /**
     * Stop browsing and advertising after each test.
     * 
     * @throws Exception
     */
    @After
    public void after() throws Exception
    {
        PlatformSDManager.getInstance().browseStop(this, "test-service1");
        resultList = null;
    }

    /**
     * Test browse.
     */
    @Test
    public void testBrowse() throws Exception
    {
        // Test invalid argument.
        Exception ex = null;
        try
        {
            PlatformSDManager.getInstance().browse(null, "test1", null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        // Test invalid argument.
        try
        {
            PlatformSDManager.getInstance().browse(this, null, null);

        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Illegal argument provided."));
        ex = null;

        // Test OK.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing("test-service1"));
            PlatformSDManager.getInstance().browse(this, "test-service1", null);
            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing("test-service1"));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Test OK, actual browse.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, null);
            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        boolean doneWithin = done.await(555, TimeUnit.SECONDS);
        Assert.assertTrue(doneWithin);
        Assert.assertNotNull(resultList);
    }

    @Override
    public void serviceResolved(List<SDEntityBrowseResult> serviceBrowseResultList)
    {
        done.countDown();
    }
}
