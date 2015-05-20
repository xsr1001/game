/**
 * @file LoggerTest.java
 * @brief LoggerTest
 */

package game.core.log;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Simple logger test.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class LoggerTest
{
    /**
     * Simple test for retrieving game logger instance.
     */
    @Test
    public void testGetLogger()
    {
        try
        {
            Logger logger = LoggerFactory.getLogger(LoggerTest.class);
            Logger logger2 = LoggerFactory.getLogger("test");

            Assert.assertNotNull(logger);
            Assert.assertEquals(logger.getName(), LoggerTest.class.getName());

            Assert.assertNotNull(logger2);
            Assert.assertEquals(logger2.getName(), "test");
        }
        catch (Exception e)
        {
            Assert.fail();
        }
    }

    /**
     * Test enter method functionality.
     */
    @Test
    @SuppressWarnings("all")
    public void testEnterExitMethod()
    {
        Exception exc = null;
        Logger logger = null;

        try
        {
            logger = LoggerFactory.getLogger(LoggerTest.class);
            Assert.assertNotNull(logger);
        }
        catch (Exception e)
        {
            Assert.fail();
        }

        // TEST SIMPLE LOGGER EXIT AND ENTER
        try
        {
            logger.enterMethod();
            logger.exitMethod();
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNull(exc);
        exc = null;

        // TEST EXIT WITH PARAMS
        try
        {
            logger.exitMethod(null, null);
            logger.exitMethod(null, "describe");
            logger.exitMethod(new Object(), null);
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNull(exc);
        exc = null;

        // TEST EXIT WITH PARAMS 2
        try
        {
            logger.exitMethod(new Object(), "Exit method description ok.");
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNull(exc);
        exc = null;

        // TEST ENTER WITH PARAMS
        try
        {
            logger.enterMethod(null);
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNotNull(exc);
        exc = null;

        try
        {
            logger.enterMethod(null, null);
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNotNull(exc);
        exc = null;

        try
        {
            logger.enterMethod(null, "Valid description");
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNull(exc);
        exc = null;

        // TEST ENTER WITH PARAMS 2
        class TestClass
        {
            @Override
            public String toString()
            {
                return "";
            }
        }
        try
        {
            logger.enterMethod(new String("Param 1"), "param 1 description", new Integer(5), "param 2 description",
                Arrays.asList(new Object[] { new Object(), new Object() }), "List description", new TestClass(),
                "Test class desc");
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNull(exc);
        exc = null;

        try
        {
            logger.enterMethod(new String("Param 1"), "param 1 description", new Integer(5), "param 2 description",
                Arrays.asList(new Object[] { new Object(), new Object() }), "List description", new TestClass());
        }
        catch (Exception e)
        {
            exc = e;
        }
        Assert.assertNotNull(exc);
        exc = null;
    }

    /**
     * Just print stuff out.
     */
    @Test
    public void testDoLog()
    {
        try
        {
            Logger logger = LoggerFactory.getLogger(LoggerTest.class);
            logger.trace("trace");
            logger.debug("debug");
            logger.info("info");
            logger.warn("warn");
            logger.error("error");
            logger.debug("debug", 1337, "second", 0.5);
            logger.debug(1338);
            logger.debug(new IllegalAccessException("ill1"));
        }
        catch (Exception e)
        {
            Assert.fail();
        }
    }

    /**
     * Test trace enabled. This tests if log configuration file is automatically picked up as by default trace is not
     * enabled.
     */
    @Test
    public void testTraceEnabled()
    {
        Assert.assertTrue(LoggerFactory.getLogger(LoggerTest.class).isTraceEnabled());
    }

}
