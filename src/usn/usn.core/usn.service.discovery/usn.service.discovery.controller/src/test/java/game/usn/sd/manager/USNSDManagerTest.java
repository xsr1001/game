/**
 * @file USNSDManagerTest.java
 * @brief <description>
 */

package game.usn.sd.manager;

import game.core.api.exception.USNException;
import game.usn.sd.endpoint.IUSNEndpoint;
import game.usn.sd.environment.IEnvironmentManager;
import game.usn.sd.listener.IServiceDiscoveryListener;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class USNSDManagerTest
{
    // Exception
    private static Exception ex;

    // Result variables.
    private static List<Inet4Address[]> hostIPv4List_result;
    private static List<Integer> hostPort_result;
    private static List<String> serviceName_result;
    private static List<Integer> shardId_result;
    private static List<Integer> groupId_result;
    private static List<String> serviceId_result;

    // UUID for service id.
    private static UUID uuid;

    // Log messages.
    private static final String MSG_REGISTER_SERVICE = "Registering service...";
    private static final String MSG_BROWSE_SERVICE = "Browsing for service...";
    private static final String MSG_WAIT_RESULT = "Waiting for browse results...";

    /**
     * Initialize before tests.
     * 
     * @throws USNException
     */
    @BeforeClass
    public static void beforeClass() throws USNException
    {
        hostIPv4List_result = new ArrayList<Inet4Address[]>();
        hostPort_result = new ArrayList<Integer>();
        serviceName_result = new ArrayList<String>();
        shardId_result = new ArrayList<Integer>();
        groupId_result = new ArrayList<Integer>();
        serviceId_result = new ArrayList<String>();

        USNSDManager.getInstance(new IEnvironmentManager() {

            @Override
            public void validateSDEndpointType(String endpointType) throws USNException
            {
                if (endpointType.equals("nope"))
                {
                    throw new USNException("nope");
                }
            }

            @Override
            public String getSDEndpointType(IUSNEndpoint endpoint) throws USNException
            {
                if (endpoint.getEndpointType().equals("admin"))
                {
                    return "_admin";
                }
                if (endpoint.getEndpointType().equals("test1"))
                {
                    return "_test1";
                }
                if (endpoint.getEndpointType().equals("test2"))
                {
                    return "_test2";
                }
                if (endpoint.getEndpointType().equals("test3"))
                {
                    return "_test3";
                }
                if (endpoint.getEndpointType().equals("test4"))
                {
                    return "_test4";
                }
                if (endpoint.getEndpointType().equals("test5"))
                {
                    return "_test5";
                }
                if (endpoint.getEndpointType().equals("test6"))
                {
                    return "_test6";
                }
                return "_game";
            }

            @Override
            public String getEnvironmentId() throws USNException
            {
                return "dev";
            }

            @Override
            public String getDomain()
            {
                return "local";
            }
        });
    }

    /**
     * Reset fields for each test.
     */
    @Before
    public void before()
    {
        ex = null;

        hostIPv4List_result.clear();
        hostPort_result.clear();
        serviceName_result.clear();
        shardId_result.clear();
        groupId_result.clear();
        serviceId_result.clear();

        uuid = null;
    }

    /**
     * Test basic browse for specific shard id and specific group id.
     */
    @Test
    public void testBrowseSpecificShardGroup()
    {
        System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_REGISTER_SERVICE));
        try
        {
            uuid = UUID.randomUUID();
            USNSDManager.getInstance(null).register(1337, "test_name", 5, 5, uuid, new IUSNEndpoint() {

                @Override
                public String getEndpointType()
                {
                    return "test1";
                }
            }, null);
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_BROWSE_SERVICE));
            USNSDManager.getInstance(null).browse("_test1", 5, 5, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    hostIPv4List_result.add(hostIPv4List);
                    hostPort_result.add(hostPort);
                    serviceName_result.add(serviceName);
                    shardId_result.add(shardId);
                    groupId_result.add(groupId);
                    serviceId_result.add(serviceId);
                }
            });

            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_WAIT_RESULT));
            Thread.sleep(5000);
            Assert.assertEquals(hostIPv4List_result.size(), 1);
            Assert.assertEquals(hostPort_result.size(), 1);
            Assert.assertEquals(serviceName_result.size(), 1);
            Assert.assertEquals(shardId_result.size(), 1);
            Assert.assertEquals(groupId_result.size(), 1);
            Assert.assertEquals(serviceId_result.size(), 1);

            Assert.assertNotNull(hostIPv4List_result.get(0));
            Assert.assertNotNull(hostPort_result.get(0));
            Assert.assertNotNull(serviceName_result.get(0));
            Assert.assertNotNull(shardId_result.get(0));
            Assert.assertNotNull(groupId_result.get(0));
            Assert.assertNotNull(serviceId_result.get(0));

            Assert.assertEquals(1337, hostPort_result.get(0).intValue());
            Assert.assertEquals(5, shardId_result.get(0).intValue());
            Assert.assertEquals(5, groupId_result.get(0).intValue());
            Assert.assertEquals("test_name", serviceName_result.get(0));
            Assert.assertEquals(uuid.toString(), serviceId_result.get(0));
            Assert.assertTrue(hostIPv4List_result.get(0).length >= 1);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test browse default shard id.
     */
    @Test
    public void testBrowseDefaultShard()
    {
        System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_REGISTER_SERVICE));
        try
        {
            uuid = UUID.randomUUID();
            USNSDManager.getInstance(null).register(1337, "test_name", 5, 5, uuid, new IUSNEndpoint() {

                @Override
                public String getEndpointType()
                {
                    return "test2";
                }
            }, null);

            USNSDManager.getInstance(null).register(1338, "test_name2", 5, 6, uuid, new IUSNEndpoint() {

                @Override
                public String getEndpointType()
                {
                    return "test2";
                }
            }, null);
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_BROWSE_SERVICE));
            USNSDManager.getInstance(null).browse("_test2", null, 5, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    hostIPv4List_result.add(hostIPv4List);
                    hostPort_result.add(hostPort);
                    serviceName_result.add(serviceName);
                    shardId_result.add(shardId);
                    groupId_result.add(groupId);
                    serviceId_result.add(serviceId);
                }
            });

            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_WAIT_RESULT));
            Thread.sleep(5000);

            Assert.assertEquals(hostIPv4List_result.size(), 1);
            Assert.assertEquals(hostPort_result.size(), 1);
            Assert.assertEquals(serviceName_result.size(), 1);
            Assert.assertEquals(shardId_result.size(), 1);
            Assert.assertEquals(groupId_result.size(), 1);
            Assert.assertEquals(serviceId_result.size(), 1);

            Assert.assertNotNull(hostIPv4List_result.get(0));
            Assert.assertNotNull(hostPort_result.get(0));
            Assert.assertNotNull(serviceName_result.get(0));
            Assert.assertNotNull(shardId_result.get(0));
            Assert.assertNotNull(groupId_result.get(0));
            Assert.assertNotNull(serviceId_result.get(0));

            Assert.assertEquals(1337, hostPort_result.get(0).intValue());
            Assert.assertEquals(5, shardId_result.get(0).intValue());
            Assert.assertEquals(5, groupId_result.get(0).intValue());
            Assert.assertEquals("test_name", serviceName_result.get(0));
            Assert.assertEquals(uuid.toString(), serviceId_result.get(0));
            Assert.assertTrue(hostIPv4List_result.get(0).length >= 1);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test default group id.
     */
    @Test
    public void testrowseDefaultGroup()
    {
        System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_REGISTER_SERVICE));
        try
        {
            uuid = UUID.randomUUID();
            USNSDManager.getInstance(null).register(1337, "test_name", 5, 5, uuid, new IUSNEndpoint() {

                @Override
                public String getEndpointType()
                {
                    return "test3";
                }
            }, null);
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_BROWSE_SERVICE));
            USNSDManager.getInstance(null).browse("_test3", 5, null, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    hostIPv4List_result.add(hostIPv4List);
                    hostPort_result.add(hostPort);
                    serviceName_result.add(serviceName);
                    shardId_result.add(shardId);
                    groupId_result.add(groupId);
                    serviceId_result.add(serviceId);
                }
            });

            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_WAIT_RESULT));
            Thread.sleep(5000);

            Assert.assertEquals(hostIPv4List_result.size(), 1);
            Assert.assertEquals(hostPort_result.size(), 1);
            Assert.assertEquals(serviceName_result.size(), 1);
            Assert.assertEquals(shardId_result.size(), 1);
            Assert.assertEquals(groupId_result.size(), 1);
            Assert.assertEquals(serviceId_result.size(), 1);

            Assert.assertNotNull(hostIPv4List_result.get(0));
            Assert.assertNotNull(hostPort_result.get(0));
            Assert.assertNotNull(serviceName_result.get(0));
            Assert.assertNotNull(shardId_result.get(0));
            Assert.assertNotNull(groupId_result.get(0));
            Assert.assertNotNull(serviceId_result.get(0));

            Assert.assertEquals(1337, hostPort_result.get(0).intValue());
            Assert.assertEquals(5, shardId_result.get(0).intValue());
            Assert.assertEquals(5, groupId_result.get(0).intValue());
            Assert.assertEquals("test_name", serviceName_result.get(0));
            Assert.assertEquals(uuid.toString(), serviceId_result.get(0));
            Assert.assertTrue(hostIPv4List_result.get(0).length >= 1);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test basic browse with default ids.
     */
    @Test
    public void testBrowseDefault()
    {
        System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_REGISTER_SERVICE));
        try
        {
            uuid = UUID.randomUUID();
            USNSDManager.getInstance(null).register(1337, "test_name", 5, 5, uuid, new IUSNEndpoint() {

                @Override
                public String getEndpointType()
                {
                    return "test4";
                }
            }, null);
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_BROWSE_SERVICE));
            USNSDManager.getInstance(null).browse("_test4", null, null, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    hostIPv4List_result.add(hostIPv4List);
                    hostPort_result.add(hostPort);
                    serviceName_result.add(serviceName);
                    shardId_result.add(shardId);
                    groupId_result.add(groupId);
                    serviceId_result.add(serviceId);
                }
            });

            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_WAIT_RESULT));
            Thread.sleep(5000);
            Assert.assertEquals(hostIPv4List_result.size(), 1);
            Assert.assertEquals(hostPort_result.size(), 1);
            Assert.assertEquals(serviceName_result.size(), 1);
            Assert.assertEquals(shardId_result.size(), 1);
            Assert.assertEquals(groupId_result.size(), 1);
            Assert.assertEquals(serviceId_result.size(), 1);

            Assert.assertNotNull(hostIPv4List_result.get(0));
            Assert.assertNotNull(hostPort_result.get(0));
            Assert.assertNotNull(serviceName_result.get(0));
            Assert.assertNotNull(shardId_result.get(0));
            Assert.assertNotNull(groupId_result.get(0));
            Assert.assertNotNull(serviceId_result.get(0));

            Assert.assertEquals(1337, hostPort_result.get(0).intValue());
            Assert.assertEquals(5, shardId_result.get(0).intValue());
            Assert.assertEquals(5, groupId_result.get(0).intValue());
            Assert.assertEquals("test_name", serviceName_result.get(0));
            Assert.assertEquals(uuid.toString(), serviceId_result.get(0));
            Assert.assertTrue(hostIPv4List_result.get(0).length >= 1);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test browse invalid params.
     */
    @Test
    public void testBrowseInvalidParams()
    {
        try
        {
            USNSDManager.getInstance(null).browse(null, null, null, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    Assert.fail();
                }
            });
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;

        try
        {
            USNSDManager.getInstance(null).browse("nope", null, null, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    Assert.fail();
                }
            });
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;

        try
        {
            USNSDManager.getInstance(null).browse("test1", null, null, null);
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
    }

    /**
     * Test basic browse stop.
     */
    @Test
    public void testBrowseStop()
    {
        System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_REGISTER_SERVICE));
        try
        {
            uuid = UUID.randomUUID();
            USNSDManager.getInstance(null).register(1337, "test_name", 5, 5, uuid, new IUSNEndpoint() {

                @Override
                public String getEndpointType()
                {
                    return "test5";
                }
            }, null);
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_BROWSE_SERVICE));

            IServiceDiscoveryListener listener = new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    hostIPv4List_result.add(hostIPv4List);
                    hostPort_result.add(hostPort);
                    serviceName_result.add(serviceName);
                    shardId_result.add(shardId);
                    groupId_result.add(groupId);
                    serviceId_result.add(serviceId);
                }
            };

            USNSDManager.getInstance(null).browse("_test5", null, null, listener);

            USNSDManager.getInstance(null).browseStop("_test5", null, null, listener);

            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_WAIT_RESULT));
            Thread.sleep(5000);
            Assert.assertEquals(hostIPv4List_result.size(), 0);
            Assert.assertEquals(hostPort_result.size(), 0);
            Assert.assertEquals(serviceName_result.size(), 0);
            Assert.assertEquals(shardId_result.size(), 0);
            Assert.assertEquals(groupId_result.size(), 0);
            Assert.assertEquals(serviceId_result.size(), 0);

        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test basic browse stop.
     */
    @Test
    public void testBrowseCache()
    {
        System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_REGISTER_SERVICE));
        try
        {
            uuid = UUID.randomUUID();
            USNSDManager.getInstance(null).register(1337, "test_name", 5, 5, uuid, new IUSNEndpoint() {

                @Override
                public String getEndpointType()
                {
                    return "test6";
                }
            }, null);
        }
        catch (USNException e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;

        try
        {
            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_BROWSE_SERVICE));
            USNSDManager.getInstance(null).browse("_test6", null, null, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    hostIPv4List_result.add(hostIPv4List);
                    hostPort_result.add(hostPort);
                    serviceName_result.add(serviceName);
                    shardId_result.add(shardId);
                    groupId_result.add(groupId);
                    serviceId_result.add(serviceId);
                }
            });

            System.out.println(String.format("%s : %s", Thread.currentThread().getStackTrace()[1], MSG_WAIT_RESULT));
            Thread.sleep(5000);
            Assert.assertEquals(hostIPv4List_result.size(), 1);
            Assert.assertEquals(hostPort_result.size(), 1);
            Assert.assertEquals(serviceName_result.size(), 1);
            Assert.assertEquals(shardId_result.size(), 1);
            Assert.assertEquals(groupId_result.size(), 1);
            Assert.assertEquals(serviceId_result.size(), 1);

            Assert.assertNotNull(hostIPv4List_result.get(0));
            Assert.assertNotNull(hostPort_result.get(0));
            Assert.assertNotNull(serviceName_result.get(0));
            Assert.assertNotNull(shardId_result.get(0));
            Assert.assertNotNull(groupId_result.get(0));
            Assert.assertNotNull(serviceId_result.get(0));

            Assert.assertEquals(1337, hostPort_result.get(0).intValue());
            Assert.assertEquals(5, shardId_result.get(0).intValue());
            Assert.assertEquals(5, groupId_result.get(0).intValue());
            Assert.assertEquals("test_name", serviceName_result.get(0));
            Assert.assertEquals(uuid.toString(), serviceId_result.get(0));
            Assert.assertTrue(hostIPv4List_result.get(0).length >= 1);

            // Browse again from cache.
            USNSDManager.getInstance(null).browse("_test6", null, null, new IServiceDiscoveryListener() {

                @Override
                public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName,
                    Integer shardId, Integer groupId, String serviceId)
                {
                    hostIPv4List_result.add(hostIPv4List);
                    hostPort_result.add(hostPort);
                    serviceName_result.add(serviceName);
                    shardId_result.add(shardId);
                    groupId_result.add(groupId);
                    serviceId_result.add(serviceId);
                }
            });
            Assert.assertEquals(hostIPv4List_result.size(), 2);
            Assert.assertEquals(hostPort_result.size(), 2);
            Assert.assertEquals(serviceName_result.size(), 2);
            Assert.assertEquals(shardId_result.size(), 2);
            Assert.assertEquals(groupId_result.size(), 2);
            Assert.assertEquals(serviceId_result.size(), 2);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }
}
