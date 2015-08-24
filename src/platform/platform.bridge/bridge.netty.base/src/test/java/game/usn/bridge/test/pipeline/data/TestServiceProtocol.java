/**
 * @file TestServiceProtocol.java
 * @brief TestServiceProtocol
 */

package game.usn.bridge.test.pipeline.data;

import platform.bridge.api.protocol.AbstractPlatformProtocol;

/**
 * Test service protocol for unit testing.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestServiceProtocol extends AbstractPlatformProtocol
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
