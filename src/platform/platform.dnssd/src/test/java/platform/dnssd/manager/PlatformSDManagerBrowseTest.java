/**
 * @file PlatformSDManagerTest.java
 * @brief Platform service discovery manager browse operation test.
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

import platform.dnssd.api.context.IPlatformSDContextManager;
import platform.dnssd.api.filter.ISDResultFilter;
import platform.dnssd.api.filter.ISDSingleResultFilter;
import platform.dnssd.api.filter.ServiceBrowseResult;
import platform.dnssd.api.listener.ISDListener;

/**
 * Platform service discovery manager browse operation test.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class PlatformSDManagerBrowseTest implements ISDListener
{
    // Field for testing fail scenario.
    private static Exception ex;

    // JmDNS manager to advertise test services.
    private static JmDNS jmDNSManager;

    // Wait mechanism for asynchronous results.
    private CountDownLatch done = new CountDownLatch(1);

    // Result list.
    private List<ServiceBrowseResult> resultList = new ArrayList<ServiceBrowseResult>();

    // Test data..
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

    /**
     * Initialized stuff before running tests.
     */
    @BeforeClass
    public static void BeforeClass() throws Exception
    {
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
        resultList.clear();
        done = new CountDownLatch(1);
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
            PlatformSDManager.getInstance().browse(null, null, null);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Service discovery manager has not been initialized yet."));
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
        Assert.assertTrue(ex.getLocalizedMessage().startsWith("Service discovery manager has not been initialized yet."));
        ex = null;
    }

    /**
     * Test browse with incorrect parameters.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseInvalid() throws Exception
    {
        // Test invalid argument.
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
    }

    /**
     * Test browse OK. Test proper flag retrieval.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseOKBrowsing() throws Exception
    {
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
    }

    /**
     * Test browse OK with found result.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseOKNotification() throws Exception
    {
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
        ServiceBrowseResult browseResult = resultList.get(0);
        Assert.assertEquals(SERVICE_INFO1.getType(), browseResult.getServiceFullType());
        Assert.assertNotNull(browseResult.getInet4AddressArray());
        Assert.assertNotNull(browseResult.getInet4AddressArray()[0]);
        Assert.assertNotNull(browseResult.getServiceContext());
        Assert.assertEquals(2, browseResult.getServiceContext().size());
        Assert.assertNotNull(browseResult.getServiceContext().get("key1"));
        Assert.assertEquals("value1", browseResult.getServiceContext().get("key1"));
    }

    /**
     * Test browse cache functionality. Second result should be received instantly after executing browse.
     * 
     * @throws Exception
     */
    @Test
    public void testbrowseCache() throws Exception
    {
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
        ServiceBrowseResult browseResult = resultList.get(0);

        /* This call to browse should result in a no-op on JmDNS manager and get cached results. */
        PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, null);
        Assert.assertEquals(2, resultList.size());
        Assert.assertNotNull(resultList.get(1));
        ServiceBrowseResult browseResultCached = resultList.get(1);
        Assert.assertEquals(browseResult.getServiceFullType(), browseResultCached.getServiceFullType());
        Assert.assertEquals(browseResult.getServiceName(), browseResultCached.getServiceName());
    }

    /**
     * Test browse result filter no results.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseResultFilterNoResult() throws Exception
    {
        // Test browse filter with null results.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, new ISDResultFilter() {

                @Override
                public List<ServiceBrowseResult> filter(List<ServiceBrowseResult> sdEntityBrowseEntryList)
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
    }

    /**
     * Test browse result filter with successful filtering.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseResultFilterResult() throws Exception
    {

        // Test browse filter with positive result
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, new ISDResultFilter() {

                @Override
                public List<ServiceBrowseResult> filter(List<ServiceBrowseResult> sdEntityBrowseEntryList)
                {
                    for (ServiceBrowseResult result : sdEntityBrowseEntryList)
                    {
                        if (result.getServiceName().equals("Check to nonexisting service name"))
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

        boolean doneWithin = false;
        if (done.getCount() == 1)
        {
            doneWithin = done.await(5, TimeUnit.SECONDS);
        }
        else
        {
            doneWithin = true;
        }

        Assert.assertTrue(doneWithin);
        Assert.assertNotNull(resultList);
        Assert.assertEquals(1, resultList.size());
        Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
    }

    /**
     * Test browse result filter with successful filtering and single entry filter.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseSingleResultFilter() throws Exception
    {

        // Test browse filter with positive result and single result filter.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, new ISDSingleResultFilter() {

                @Override
                public List<ServiceBrowseResult> filter(List<ServiceBrowseResult> sdEntityBrowseEntryList)
                {
                    for (ServiceBrowseResult result : sdEntityBrowseEntryList)
                    {
                        if (result.getServiceName().equals("Check to nonexisting service name"))
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
        Assert.assertNotNull(resultList);
        Assert.assertEquals(1, resultList.size());

        // No more browsing because single result filter removed listener automatically.
        Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
    }

    /**
     * Test browse stop invalid input.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseStopInvalidInput() throws Exception
    {
        // Browse stop invalid input.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browseStop(null, null);
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;

        // Browse stop non existent entity service type.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing("some random service type."));
            PlatformSDManager.getInstance().browseStop(this, "some random service type.");
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing("some random service type."));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test browse stop multiple listeners.
     * 
     * @throws Exception
     */
    @Test
    public void testBrowseStopMultipleListeners() throws Exception
    {
        ISDListener toRemoveListener = new ISDListener() {

            @Override
            public void serviceResolved(List<ServiceBrowseResult> serviceBrowseResultList)
            {
                done.countDown();
                resultList.addAll(serviceBrowseResultList);
            }
        };

        // Browse with listener 1.
        try
        {
            Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
            PlatformSDManager.getInstance().browse(toRemoveListener, TEST_SERVICE_TYPE1, null);
            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Browse with listener 2.
        try
        {
            PlatformSDManager.getInstance().browse(this, TEST_SERVICE_TYPE1, null);
            Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Stop browsing with listener 1.
        try
        {
            PlatformSDManager.getInstance().browseStop(toRemoveListener, TEST_SERVICE_TYPE1);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Should be still browsing.
        Assert.assertTrue(PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
        boolean doneWithin = false;
        if (done.getCount() == 1)
        {
            doneWithin = done.await(10, TimeUnit.SECONDS);
        }
        else
        {
            doneWithin = true;
        }

        // Should get results.
        Assert.assertTrue(doneWithin);
        Assert.assertNotNull(resultList);

        // Stop browsing with listener 2.
        try
        {
            PlatformSDManager.getInstance().browseStop(this, TEST_SERVICE_TYPE1);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        // Shouldn't be listening anymore.
        Assert.assertTrue(!PlatformSDManager.getInstance().isBrowsing(TEST_SERVICE_TYPE1));
    }

    /**
     * {@inheritDoc} Set results to the local list to assert.
     */
    @Override
    public void serviceResolved(List<ServiceBrowseResult> serviceBrowseResultList)
    {
        done.countDown();
        resultList.addAll(serviceBrowseResultList);
    }
}
