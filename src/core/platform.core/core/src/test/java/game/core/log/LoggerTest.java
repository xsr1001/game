/**
 * @file LogTest.java
 * @brief LogTest
 */

package game.core.log;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Simple logger test.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class LoggerTest
{
    /**
     * Simple test for retrieving logger instance.
     */
    @Test
    public void testGetLogger()
    {
        try
        {
            LoggerFactory.getLogger(LoggerTest.class);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
    }
}
