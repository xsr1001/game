/**
 * @file TestPacket2.java
 * @brief TestPacket2.
 */

package game.usn.bridge.test.pipeline.data;

import game.usn.bridge.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;

/**
 * Test packet for Unit tests. This packet does not correctly implement write and read methods.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestPacket2 extends AbstractPacket
{
    // Test String field.
    private String testString;

    /**
     * Ctor.
     */
    public TestPacket2()
    {
        this.testString = "hello-world";
    }

    @Override
    public void write(ByteBuf buf)
    {

    }

    @Override
    public void read(ByteBuf buf)
    {

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
