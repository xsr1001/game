/**
 * @file AbstractPacketTest.java
 * @brief Test for base packet functionality.
 */

package game.usn.bridge.api.test.protocol;

import game.usn.bridge.api.test.protocol.data.TestPacket;
import game.usn.bridge.api.test.protocol.data.TestPacket2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.proxy.transport.ITransportIdentifiable;

/**
 * Test packet fields de/serialization.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestAbstractPacket
{
    // Public test data.
    public static final PacketP1 PACKET1_ID = new PacketP1();
    public static final PacketP2 PACKET2_ID = new PacketP2();
    public static final AbstractPacket PACKET3 = new PacketP3();
    public static final AbstractPacket PACKET4 = new PacketP4();

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

    /**
     * Test packet 1. Transport identifiable.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class PacketP1 extends AbstractPacket implements ITransportIdentifiable
    {
        private String testString = "test1";
        private UUID uuid;

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
        }

        @Override
        public void setTransportId(UUID id)
        {
            uuid = id;

        }

        @Override
        public UUID getTransportId()
        {
            return uuid;
        }
    }

    /**
     * Test packet 2. Transport identifiable.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class PacketP2 extends AbstractPacket implements ITransportIdentifiable
    {
        private String testString = "test2";
        private int bla = 2;
        private UUID uuid;

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
            bla = readInt(buf);
        }

        @Override
        public void setTransportId(UUID id)
        {
            uuid = id;

        }

        @Override
        public UUID getTransportId()
        {
            return uuid;
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
            writeInt(bla, buf);
        }
    }

    /**
     * Test packet 3. Not transport identifiable.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class PacketP3 extends AbstractPacket
    {
        private String testString = "test2";
        private int bla = 2;

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
            bla = readInt(buf);
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
            writeInt(bla, buf);
        }
    }

    /**
     * Test packet 4. Not transport identifiable.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public static class PacketP4 extends AbstractPacket
    {
        private String testString = "test2";
        private int bla = 2;

        public void read(ByteBuf buf)
        {
            testString = readString(buf);
            bla = readInt(buf);
        }

        public void write(ByteBuf buf)
        {
            writeString(buf, testString);
            writeInt(bla, buf);
        }
    }
}
