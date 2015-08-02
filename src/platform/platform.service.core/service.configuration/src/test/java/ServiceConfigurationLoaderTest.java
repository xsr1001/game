/**
 * @file ServiceConfigurationLoaderTest.java
 * @brief Test configuration loader functionality.
 */

import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import platform.service.configuration.ServiceConfigurationLoader;
import platform.service.configuration.schema.ServiceConfiguration;
import platform.service_two.configuration.schema.AdminServiceConfiguration100;

/**
 * Test configuration loader functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ServiceConfigurationLoaderTest
{
    /**
     * Test raw configuration load without custom service configuration - no additional custom elements.
     */
    @Test
    public void testValidConfigurationLoadBare()
    {
        Exception ex = null;
        try
        {
            ServiceConfiguration config = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/service1_configuration_valid.xml", true);

            Assert.assertNotNull(config);
            Assert.assertEquals("platform.sd.context.manager.impl.DevSDContextManager",
                config.getValue().getPlatform().getSDContext().getContextManagerClassBinding());
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test extended configuration load with custom service configuration.
     */
    @Test
    public void testValidConfigurationLoadExtended()
    {
        Exception ex = null;
        try
        {
            ServiceConfiguration config = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/service2_configuration_valid.xml", true);

            Assert.assertNotNull(config);
            Assert.assertEquals("platform.sd.context.manager.impl.DevSDContextManager",
                config.getValue().getPlatform().getSDContext().getContextManagerClassBinding());

            Assert.assertTrue(config.getValue().getService().getConfiguration() instanceof AdminServiceConfiguration100);
            AdminServiceConfiguration100 specificConfiguration = (AdminServiceConfiguration100) config.getValue().getService().getConfiguration();
            Assert.assertNotNull(specificConfiguration);
            Assert.assertEquals("test_type",
                specificConfiguration.getValue().getAdminServiceSpecificElement().getAdminType());
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test configuration caching functionality.
     */
    @Test
    public void testCache()
    {
        Exception ex = null;
        try
        {
            ServiceConfiguration config = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/service2_configuration_valid.xml", true);

            Assert.assertNotNull(config);
            Assert.assertTrue(config.getValue().getService().getConfiguration() instanceof AdminServiceConfiguration100);

            ServiceConfiguration cachedConfiguration = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/service1_configuration_valid.xml", false);
            Assert.assertNotNull(cachedConfiguration);
            Assert.assertTrue(cachedConfiguration.getValue().getService().getConfiguration() instanceof AdminServiceConfiguration100);
            Assert.assertEquals(cachedConfiguration, config);

            ServiceConfiguration overrideConfiguration = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/service1_configuration_valid.xml", true);
            Assert.assertNotNull(overrideConfiguration);
            Assert.assertTrue(overrideConfiguration.getValue().getService().getConfiguration() instanceof platform.service_one.configuration.schema.AdminServiceConfiguration100);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test configuration file with absolute path load. Invalid and valid absolute path.
     */
    @Test
    public void testAbsoluteFilePathLoad()
    {
        URL absolutePath = ServiceConfigurationLoader.class.getResource("/service2_configuration_valid.xml");
        Assert.assertTrue(!absolutePath.getPath().isEmpty());

        Exception ex = null;
        try
        {
            ServiceConfigurationLoader.getInstance().loadConfiguration(
                absolutePath.getPath().concat("non-existing-path"), true);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;

        try
        {
            ServiceConfiguration config = ServiceConfigurationLoader.getInstance().loadConfiguration(
                absolutePath.getPath(), true);
            Assert.assertNotNull(config);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);
        ex = null;
    }

    /**
     * Test missing invalid custom configuration element.
     */
    @Test
    public void testMissingCustomConfiguration()
    {

        Exception ex = null;
        try
        {
            ServiceConfigurationLoader.getInstance().loadConfiguration("/service1_configuration_invalid.xml", true);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
    }

    /**
     * Test missing invalid global configuration parameter.
     */
    @Test
    public void testMissingGlobalConfigurationPatameter()
    {

        Exception ex = null;
        try
        {
            ServiceConfigurationLoader.getInstance().loadConfiguration();
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
    }

    /**
     * Test invalid input parameters.
     */
    @Test
    public void testInvalidParameters()
    {

        Exception ex = null;
        try
        {
            ServiceConfigurationLoader.getInstance().loadConfiguration(null, true);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        ex = null;
    }
}
