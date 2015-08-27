/**
 * @file TestPacket.java
 * @brief TestPacket.
 */

package game.usn.bridge.api.test.protocol.data;

import platform.bridge.api.protocol.AbstractPacket;
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
    private long testLong;
    private int testInt;
    private short testShort;

    private long testLong2;
    private int testInt2;
    private short testShort2;

    /**
     * Ctor.
     */
    public TestPacket()
    {
        this.testString = "hello-world";
        this.testInt = 5;
        this.testLong = 4;
        this.testShort = 3;

        this.testInt2 = Integer.MAX_VALUE;
        this.testLong2 = Integer.MAX_VALUE;
        this.testShort2 = Short.MAX_VALUE;
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString(buf, this.testString);
        writeInt((int) this.testLong, buf);
        writeInt((int) this.testShort, buf);
        writeInt(this.testInt, buf);

        writeInt((int) this.testLong2, buf);
        writeInt((int) this.testShort2, buf);
        writeInt(this.testInt2, buf);
    }

    @Override
    public void read(ByteBuf buf)
    {
        this.testString = readString(buf);
        this.testLong = readInt(buf);
        this.testShort = (short) readInt(buf);
        this.testInt = readInt(buf);

        this.testLong2 = readInt(buf);
        this.testShort2 = (short) readInt(buf);
        this.testInt2 = readInt(buf);
    }

    public String getTestString()
    {
        return testString;
    }

    public void setTestString(String testString)
    {
        this.testString = testString;
    }

    public long getTestLong()
    {
        return testLong;
    }

    public void setTestLong(long testLong)
    {
        this.testLong = testLong;
    }

    public int getTestInt()
    {
        return testInt;
    }

    public void setTestInt(int testInt)
    {
        this.testInt = testInt;
    }

    public short getTestShort()
    {
        return testShort;
    }

    public void setTestShort(short testShort)
    {
        this.testShort = testShort;
    }

    public long getTestLong2()
    {
        return testLong2;
    }

    public void setTestLong2(long testLong2)
    {
        this.testLong2 = testLong2;
    }

    public int getTestInt2()
    {
        return testInt2;
    }

    public void setTestInt2(int testInt2)
    {
        this.testInt2 = testInt2;
    }

    public short getTestShort2()
    {
        return testShort2;
    }

    public void setTestShort2(short testShort2)
    {
        this.testShort2 = testShort2;
    }

}
