/**
 * @file TestUSNPacketEncoder.java
 * @brief Test pipeline protocol encoder.
 */

package game.usn.bridge.test.pipeline;

import game.usn.bridge.test.e2e.data.UnknownPacket;
import game.usn.bridge.test.pipeline.data.TestPacket;
import game.usn.bridge.test.pipeline.data.TestServiceProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.xml.ws.ProtocolException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import platform.bridge.base.pipeline.encoder.PlatformPacketEncoder;

/**
 * Test pipeline protocol encoder.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestUSNPacketEncoder
{
    // Reusable fields.
    private Exception ex;
    private PlatformPacketEncoder encoder;
    private ByteBuf buffer = Unpooled.buffer();

    /**
     * Reset before each test.
     */
    @Before
    public void beforeTest()
    {
        this.ex = null;
        this.encoder = new PlatformPacketEncoder(new TestServiceProtocol());
        this.buffer.clear();
    }

    /**
     * Simple test for decoder instantiation.
     */
    @Test
    public void testInstantiation()
    {
        try
        {
            new PlatformPacketEncoder(null);
        }
        catch (Exception e)
        {
            this.ex = e;
        }

        Assert.assertNotNull(this.ex);
        Assert.assertTrue(this.ex instanceof IllegalArgumentException);

        Assert.assertNotNull(this.encoder);
    }

    /**
     * Test encode operation for unsupported message id.
     */
    @Test
    public void testUnsupportedMessageId()
    {
        UnknownPacket packet = new UnknownPacket();
        try
        {
            this.encoder.encode(null, packet, buffer);
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNotNull(this.ex);
        Assert.assertTrue(this.ex instanceof ProtocolException);
    }

    /**
     * Test encode ok.
     */
    @Test
    public void testOKEncode()
    {
        TestPacket packet = new TestPacket();
        try
        {
            this.encoder.encode(null, packet, buffer);
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNull(this.ex);
        Assert.assertNotNull(this.buffer);
        Assert.assertEquals(this.buffer.readByte(), 0b0000_0001);
    }
}
