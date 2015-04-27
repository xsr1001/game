/**
 * @file ArgsCheckerTest.java
 * @brief Simple test for args checker functionality.
 */

package game.core.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Args checker test.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ArgsCheckerTest
{
    /**
     * Test provided null description to checker.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErrorOnNull_badDescription()
    {
        ArgsChecker.errorOnNull(new Object(), null);
    }

    /**
     * Test provided null object to checker.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErrorOnNull_badObject()
    {
        ArgsChecker.errorOnNull(null, new String("Test object description."));
    }

    /**
     * Test ok.
     */
    @Test
    public void testErrorOnNull_OK()
    {
        try
        {
            ArgsChecker.errorOnNull(new String("Test object."), new String("Test object description."));
        }
        catch (IllegalArgumentException ie)
        {
            Assert.fail();
        }
    }
}
