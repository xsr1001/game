/**
 * @file ServiceConfigurationLoaderTest.java
 * @brief Test configuration loader functionality.
 */

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
}
