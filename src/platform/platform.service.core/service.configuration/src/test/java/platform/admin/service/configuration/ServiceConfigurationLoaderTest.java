/**
 * @file ServiceConfigurationLoaderTest.java
 * @brief <description>
 */

package platform.admin.service.configuration;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import platform.service.configuration.ServiceConfigurationLoader;
import platform.service.configuration.schema.ServiceConfiguration;

public class ServiceConfigurationLoaderTest
{
    private Exception ex;

    @Before
    public void before()
    {
        ex = null;
    }

    @Test
    public void testValidConfigurationLoad()
    {
        try
        {
            ServiceConfiguration config = ServiceConfigurationLoader.getInstance().loadConfiguration(
                "/valid_config.xml");
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
