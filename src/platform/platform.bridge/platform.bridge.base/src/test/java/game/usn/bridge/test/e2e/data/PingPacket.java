/**
 * @file PingPacket.java
 * @brief <description>
 */

package game.usn.bridge.test.e2e.data;

import game.usn.bridge.api.protocol.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class PingPacket extends AbstractPacket
{
    // Test String field.
    private String testString;

    /**
     * Ctor.
     */
    public PingPacket()
    {
        this.testString = "ping";
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
