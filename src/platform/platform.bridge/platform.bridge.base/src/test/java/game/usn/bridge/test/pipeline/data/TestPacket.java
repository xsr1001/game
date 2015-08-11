/**
 * @file TestPacket.java
 * @brief TestPacket.
 */

package game.usn.bridge.test.pipeline.data;

import game.usn.bridge.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;

/**
 * Test packet for Unit tests.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestPacket extends AbstractPacket
{
    // Test String field.
    private String testString;

    /**
     * Ctor.
     */
    public TestPacket()
    {
        this.testString = "hello-world";
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString(buf, this.testString);
    }

    @Override
    public void read(ByteBuf buf)
    {
        this.testString = readString(buf);
    }

    public String getTestString()
    {
        return testString;
    }

    public void setTestString(String testString)
    {
        this.testString = testString;
    }
}
