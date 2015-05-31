/**
 * @file UnknownPacket.java
 * @brief UnknownPacket.
 */

package game.usn.bridge.test.data;

import game.usn.bridge.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;

/**
 * Test packet for Unit tests. This packet is not registered with consumer protocol.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class UnknownPacket extends AbstractPacket
{
    // Test String field.
    private String testString;

    /**
     * Ctor.
     */
    public UnknownPacket()
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
