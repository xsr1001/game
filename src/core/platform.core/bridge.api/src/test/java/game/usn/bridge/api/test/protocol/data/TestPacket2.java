/**
 * @file TestPacket2.java
 * @brief TestPacket2.
 */

package game.usn.bridge.api.test.protocol.data;

import game.usn.bridge.api.protocol.AbstractPacket;

/**
 * Test packet for Unit tests.
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

    public String getTestString()
    {
        return testString;
    }

    public void setTestString(String testString)
    {
        this.testString = testString;
    }
}
