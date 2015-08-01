/**
 * @file AbstractPacketTest.java
 * @brief Test for base packet functionality.
 */

package game.usn.bridge.api.test.protocol;

import game.usn.bridge.api.test.protocol.data.TestPacket;
import game.usn.bridge.api.test.protocol.data.TestPacket2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Test packet fields de/serialization.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestAbstractPacket
{
    // Reusable fields.
    private Exception ex;

    /**
     * Reset fields.
     */
    @Before
    public void beforeTest()
    {
        this.ex = null;
    }

    /**
     * Test basic read write implementation.
     */
    @Test
    public void testReadWrite()
    {
        try
        {
            new TestPacket().read(Unpooled.buffer(1024));
            new TestPacket().write(Unpooled.buffer(1024));
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IndexOutOfBoundsException);

        try
        {
            new TestPacket2().read(Unpooled.buffer(1024));
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof UnsupportedOperationException);

        try
        {
            new TestPacket2().write(Unpooled.buffer(1024));
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof UnsupportedOperationException);
    }

    /**
     * Test all write and read operations.
     */
    @Test
    public void testWrite()
    {
        TestPacket packet = new TestPacket();
        TestPacket packet2 = new TestPacket();

        ByteBuf buf = Unpooled.buffer(1024 * 1024);
        try
        {
            packet.write(buf);
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNull(ex);

        try
        {
            packet.read(buf);
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNull(ex);

        Assert.assertEquals(packet.getTestInt(), packet2.getTestInt());
        Assert.assertEquals(packet.getTestLong(), packet2.getTestLong());
        Assert.assertEquals(packet.getTestShort(), packet2.getTestShort());
        Assert.assertEquals(packet.getTestString(), packet2.getTestString());

        Assert.assertEquals(packet.getTestInt2(), packet2.getTestInt2());
        Assert.assertEquals(packet.getTestLong2(), packet2.getTestLong2());
        Assert.assertEquals(packet.getTestShort2(), packet2.getTestShort2());
    }
}
