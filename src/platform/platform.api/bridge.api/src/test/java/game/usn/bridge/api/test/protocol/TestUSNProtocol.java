/**
 * @file TestUSNProtocol.java
 * @brief Test for base protocol handling.
 */

package game.usn.bridge.api.test.protocol;

import game.usn.bridge.api.test.protocol.data.TestPacket;
import game.usn.bridge.api.test.protocol.data.TestPacket2;
import game.usn.bridge.api.test.protocol.data.TestProtocol1;
import game.usn.bridge.api.test.protocol.data.TestProtocol2;

import javax.xml.ws.ProtocolException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import platform.bridge.api.protocol.AbstractPlatformProtocol;

/**
 * Test for base protocol handling.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestUSNProtocol
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
     * Test custom frame size.
     */
    @Test
    public void testFrameSize()
    {
        Assert.assertEquals(new TestProtocol1().getFrameLengthHeaderSize(),
            AbstractPlatformProtocol.DEFAULT_FRAME_LENGTH_HEADER_SIZE);
        Assert.assertEquals(new TestProtocol2(666).getFrameLengthHeaderSize(), 666);
    }

    /**
     * Test packet registered.
     */
    @Test
    public void testPacketRegistered()
    {
        TestProtocol2 prot2 = new TestProtocol2(66);
        Assert.assertFalse(prot2.packetRegistered(TestPacket2.class));
        Assert.assertFalse(prot2.packetRegistered(6));
        Assert.assertTrue(prot2.packetRegistered(1));
        Assert.assertTrue(prot2.packetRegistered(TestPacket.class));
    }

    /**
     * Test get packet id.
     */
    @Test
    public void testGetPacketId()
    {
        TestProtocol2 prot2 = new TestProtocol2(66);
        try
        {
            Assert.assertEquals(prot2.getPacketId(TestPacket.class), 1);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);

        try
        {
            prot2.getPacketId(TestPacket2.class);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof ProtocolException);
    }

    /**
     * Test construct packet.
     */
    @Test
    public void testConstructPacket()
    {
        TestProtocol2 prot2 = new TestProtocol2(66);

        try
        {
            Assert.assertTrue(prot2.constructPacket(1) instanceof TestPacket);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNull(ex);

        try
        {
            prot2.constructPacket(1337);
        }
        catch (Exception e)
        {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof ProtocolException);
    }
}
