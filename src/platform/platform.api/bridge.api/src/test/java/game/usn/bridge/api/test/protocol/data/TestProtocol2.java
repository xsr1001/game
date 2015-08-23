/**
 * @file TestProtocol2.java
 * @brief TestProtocol2
 */

package game.usn.bridge.api.test.protocol.data;

import game.usn.bridge.api.protocol.AbstractPlatformProtocol;

/**
 * Test protocol instance 2. Register some consumer specific packets.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class TestProtocol2 extends AbstractPlatformProtocol
{
    public TestProtocol2(int frameSize)
    {
        super(frameSize);
        registerPacket(1, TestPacket.class);
    }
}
