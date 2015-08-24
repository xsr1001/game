/**
 * @file PongPacket.java
 * @brief <description>
 */

package game.usn.bridge.test.e2e.data;

import platform.bridge.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class PongPacket extends AbstractPacket
{
    // Test String field.
    private String testString;

    /**
     * Ctor.
     */
    public PongPacket()
    {
        this.testString = "pong";
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
