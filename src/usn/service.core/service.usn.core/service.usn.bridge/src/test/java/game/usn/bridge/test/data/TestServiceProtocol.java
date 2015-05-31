/**
 * @file TestServiceProtocol.java
 * @brief TestServiceProtocol
 */

package game.usn.bridge.test.data;

import game.usn.bridge.api.protocol.AbstractUSNProtocol;

/**
 * Test service protocol for unit testing.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestServiceProtocol extends AbstractUSNProtocol
{
    /**
     * Register test packets.
     */
    public TestServiceProtocol()
    {
        registerPacket(1, TestPacket.class);
        registerPacket(2, TestPacket2.class);
    }
}
