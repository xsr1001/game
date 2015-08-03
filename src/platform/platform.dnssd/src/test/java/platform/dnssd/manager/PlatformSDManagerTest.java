/**
 * @file PlatformSDManagerTest.java
 * @brief <description>
 */

package platform.dnssd.manager;

import java.util.ArrayList;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import platform.dnssd.api.IPlatformSDContextManager;
import platform.dnssd.api.filter.ISDResultFilter;
import platform.dnssd.api.filter.ISDSingleResultFilter;
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

    // Test service to advertise.
    private static final ServiceInfo SERVICE_INFO1 = ServiceInfo.create(
        "_".concat(TEST_SERVICE_TYPE1).concat("._tcp.").concat(TEST_PLATFORM_CONTEXT_MANAGER.getPlatformId()).concat(
            ".").concat(TEST_PLATFORM_CONTEXT_MANAGER.getDomain()).concat("."), TEST_SERVICE_NAME1, TEST_SERVICE_PORT1,
        0, 0, true, propertyMap);

    // JmDNS manager to advertise test services.
    private static JmDNS jmDNSManager;

    // Wait mechanism for asynchronous results.
    private final CountDownLatch done = new CountDownLatch(1);

    // Result list.
    private List<SDEntityBrowseResult> resultList = new ArrayList<SDEntityBrowseResult>();

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
     * Initialize platform manager before each test.
     */
    @Before
    public void before() throws Exception
    {
        PlatformSDManager.getInstance().init(TEST_PLATFORM_CONTEXT_MANAGER);
    }

    /**
     * Shut down platform SD manager after each test.
     * 
     * @throws Exception
     */
    @After
    public void after() throws Exception
    {
        PlatformSDManager.getInstance().shutdown();
        resultList.clear();
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

        boolean doneWithin = false;
        if (done.getCount() == 1)
        {
            doneWithin = done.await(10, TimeUnit.SECONDS);
        }
        else
        {
            doneWithin = true;
        }
        Assert.assertTrue(doneWithin);
        Assert.assertNotNull(resultList);

        Assert.assertEquals(1, resultList.size());
        Assert.assertNotNull(resultList.get(0));
        SDEntityBrowseResult browseResult = resultList.get(0);
        Assert.assertEquals(SERVICE_INFO1.getType(), browseResult.getType());
        Assert.assertNotNull(browseResult.getInet4AddressArray());
        Assert.assertNotNull(browseResult.getInet4AddressArray()[0]);
        Assert.assertNotNull(browseResult.getSdEntityContext());
        Assert.assertEquals(2, browseResult.getSdEntityContext().size());
        Assert.assertNotNull(browseResult.getSdEntityContext().get("key1"));
        Assert.assertEquals("value1", browseResult.getSdEntityContext().get("key1"));
    }

    /**
     * Test browse caching functionality. Second result should be received instantly after executing browse.
     * 
     * @throws Exception
     */
    @Test
    public void testbrowseCache() throws Exception
    {
        Exception ex = null;

        // Browse for results.
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

        boolean doneWithin = false;
        if (done.getCount() == 1)
        {
            doneWithin = done.await(10, TimeUnit.SECONDS);
        }
        else
        {
            doneWithin = true;
        }
        Assert.assertTrue(doneWithin);
        Assert.assertNotNull(resultList);
        Assert.assertEquals(1, resultList.size());
        Assert.assertNotNull(resultList.get(0));
        SDEntityBrowseResult browseResult = resultList.get(0);

        /* This call to provse should result in a no-op on JmDNS manager and get cached results. */
        PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, null);
        Assert.assertEquals(2, resultList.size());
        Assert.assertNotNull(resultList.get(1));
        SDEntityBrowseResult browseResultCached = resultList.get(1);
        Assert.assertEquals(browseResult.getType(), browseResultCached.getType());
        Assert.assertEquals(browseResult.getName(), browseResultCached.getName());
    }

    /**
     * Test browse result filter functionality.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseResultFilter() throws Exception
    {
        Exception ex = null;

        // Test browse filter with null results.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, new ISDResultFilter() {

                @Override
                public List<SDEntityBrowseResult> filter(List<SDEntityBrowseResult> sdEntityBrowseEntryList)
                {
                    return null;
                }
            });

            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        boolean doneWithin = false;
        if (done.getCount() == 1)
        {
            doneWithin = done.await(5, TimeUnit.SECONDS);
        }
        else
        {
            doneWithin = true;
        }
        Assert.assertFalse(doneWithin);
        Assert.assertNotNull(resultList);
        Assert.assertEquals(0, resultList.size());

        // Test browse filter with positive result
        try
        {
            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, new ISDResultFilter() {

                @Override
                public List<SDEntityBrowseResult> filter(List<SDEntityBrowseResult> sdEntityBrowseEntryList)
                {
                    for (SDEntityBrowseResult result : sdEntityBrowseEntryList)
                    {
                        if (result.getName().equals("Check to nonexisting service name"))
                        {
                            sdEntityBrowseEntryList.remove(result);
                        }
                    }

                    return sdEntityBrowseEntryList;
                }
            });
            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Result should be instant because they will get cached.
        Assert.assertNotNull(resultList);
        Assert.assertNotNull(resultList);
        Assert.assertEquals(1, resultList.size());
        Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));

        resultList.clear();

        // Test browse filter with positive result and single entry filter.
        try
        {
            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, new ISDSingleResultFilter() {

                @Override
                public List<SDEntityBrowseResult> filter(List<SDEntityBrowseResult> sdEntityBrowseEntryList)
                {
                    for (SDEntityBrowseResult result : sdEntityBrowseEntryList)
                    {
                        if (result.getName().equals("Check to nonexisting service name"))
                        {
                            sdEntityBrowseEntryList.remove(result);
                        }
                    }

                    return sdEntityBrowseEntryList;
                }
            });
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Result should be instant because they will get cached.
        Assert.assertNotNull(resultList);
        Assert.assertNotNull(resultList);
        Assert.assertEquals(1, resultList.size());
        Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
    }

    /**
     * {@inheritDoc} Set results to the local list to assert.
     */
    @Override
    public void serviceResolved(List<SDEntityBrowseResult> serviceBrowseResultList)
    {
        done.countDown();
        resultList.addAll(serviceBrowseResultList);
    }
}
