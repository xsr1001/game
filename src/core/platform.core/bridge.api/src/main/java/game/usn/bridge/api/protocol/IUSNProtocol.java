/**
 * @file IUSNProtocol.java
 * @brief <description>
 */

package game.usn.bridge.api.protocol;

public interface IUSNProtocol
{
    @Override
    String toString();

    int getFrameSize();

    boolean supportsPacket(short packetId);

    AbstractPacket createPacket(short packetId);
}
