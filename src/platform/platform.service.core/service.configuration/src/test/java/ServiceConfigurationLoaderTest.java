/**
 * @file ServiceConfigurationLoaderTest.java
 * @brief Test configuration loader functionality.
 */


import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import platform.service.configuration.ServiceConfigurationLoader;
import platform.service.configuration.schema.ServiceConfiguration;

/**
 * Test configuration loader functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ServiceConfigurationLoaderTest
{
    // Exception field for test assertion.
    private Exception ex;

    /**
     * Reset field before each test.
     */
    @Before
    public void before()
    {
        ex = null;
    }

    /**
     * Test basic raw configuration load without custom service configuration.
     */
    @Test
    public void testValidConfigurationLoadBare()
    {
        try
        {
            ServiceConfiguration config = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/AS_valid_config1.xml");

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
        try
        {
            ServiceConfiguration config = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/AS_valid_config2.xml");

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
}
