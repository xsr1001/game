/**
 * @file PlatformSDContextManagerUnitTest.java
 * @brief Define some simple tests just to double check if new SD manager has been added correctly.
 */

package platform.dnssd.context;

import java.lang.reflect.Modifier;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Define some simple tests just to double check if new SD manager has been added correctly.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class PlatformSDContextManagerUnitTest
{
    /**
     * Test if platform SD context managers are declared as final.
     */
    @Test
    public void testManagersFinal()
    {
        Assert.assertTrue(Modifier.isFinal(PlatformSDContextManagerDev.class.getModifiers()));
        Assert.assertTrue(Modifier.isFinal(PlatformSDContextManagerProd.class.getModifiers()));
        Assert.assertTrue(Modifier.isFinal(PlatformSDContextManagerTest.class.getModifiers()));
    }

    /**
     * Test for non null domain retrieval.
     */
    @Test
    public void testGetDomain()
    {
        Assert.assertNotNull(new PlatformSDContextManagerDev().getDomain());
        Assert.assertNotNull(new PlatformSDContextManagerProd().getDomain());
        Assert.assertNotNull(new PlatformSDContextManagerTest().getDomain());
    }

    /**
     * Test for non null platform id retrieval.
     */
    @Test
    public void testGetPlatformId()
    {
        Assert.assertNotNull(new PlatformSDContextManagerDev().getPlatformId());
        Assert.assertNotNull(new PlatformSDContextManagerProd().getPlatformId());
        Assert.assertNotNull(new PlatformSDContextManagerTest().getPlatformId());
    }
}
