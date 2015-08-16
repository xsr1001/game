/**
 * @file TestUSNPacketDecoder.java
 * @brief Test pipeline protocol decoder.
 */

package game.usn.bridge.test.pipeline;

import game.usn.bridge.pipeline.decoder.USNPacketDecoder;
import game.usn.bridge.test.pipeline.data.TestPacket;
import game.usn.bridge.test.pipeline.data.TestPacket2;
import game.usn.bridge.test.pipeline.data.TestServiceProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.ProtocolException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Test pipeline protocol decoder.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestUSNPacketDecoder
{
    // Reusable fields.
    private Exception ex;
    private List<Object> outObjectList = new ArrayList<Object>();
    private USNPacketDecoder decoder;
    private ByteBuf buffer = Unpooled.buffer();

    /**
     * Reset before each test.
     */
    @Before
    public void beforeTest()
    {
        this.ex = null;
        this.outObjectList.clear();
        this.decoder = new USNPacketDecoder(new TestServiceProtocol());
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
            new USNPacketDecoder(null);
        }
        catch (Exception e)
        {
            this.ex = e;
        }

        Assert.assertNotNull(this.ex);
        Assert.assertTrue(this.ex instanceof IllegalArgumentException);

        Assert.assertNotNull(this.decoder);
    }

    /**
     * Test decode operation for unsupported message id.
     */
    @Test
    public void testUnsupportedMessageId()
    {
        this.buffer.writeByte(0b0000_1111);
        new TestPacket().write(this.buffer);

        try
        {
            this.decoder.decode(null, this.buffer, this.outObjectList);
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNotNull(this.ex);
        Assert.assertTrue(this.ex instanceof ProtocolException);
    }

    /**
     * Test decode ok.
     */
    @Test
    public void testOKDecode()
    {
        this.buffer.writeByte(new TestServiceProtocol().getPacketId(TestPacket.class));

        TestPacket testPacket = new TestPacket();
        testPacket.setTestString("test1");
        testPacket.write(this.buffer);

        try
        {
            this.decoder.decode(null, this.buffer, this.outObjectList);
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNull(this.ex);
        Assert.assertEquals(this.outObjectList.size(), 1);
        Assert.assertNotNull(this.outObjectList.get(0));
        Assert.assertTrue(this.outObjectList.get(0) instanceof TestPacket);
        Assert.assertNotNull(((TestPacket) this.outObjectList.get(0)).getTestString());
        Assert.assertEquals(((TestPacket) this.outObjectList.get(0)).getTestString(), "test1");
    }

    /**
     * Test decode fail to read all bytes.
     */
    @Test
    public void testDecodeFailReadBytes()
    {
        this.buffer.writeByte(new TestServiceProtocol().getPacketId(TestPacket2.class));
        new TestPacket().write(this.buffer);

        try
        {
            this.decoder.decode(null, this.buffer, this.outObjectList);
        }
        catch (Exception e)
        {
            this.ex = e;
        }
        Assert.assertNotNull(this.ex);
        Assert.assertTrue(this.ex instanceof ProtocolException);
    }
}
