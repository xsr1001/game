/**
 * @file ITestTransportObserver.java
 * @brief Provides capabilities for a test suite to register for transport level notifications.
 */

package game.usn.bridge.test.e2e.testdata;

import platform.bridge.api.protocol.AbstractPacket;

/**
 * Provides capabilities for a test suite to register for transport level notifications.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface ITestTransportObserver
{
    /**
     * Notification that client has successfully sent a packet.
     * 
     * @param abstractPacket
     *            - a sent {@link AbstractPacket} packet.
     */
    void clientSent(AbstractPacket abstractPacket);

    /**
     * Notification that client has received a response from the server.
     * 
     * @param abstractPacket
     *            - a received {@link AbstractPacket} packet.
     */
    void clientReceived(AbstractPacket abstractPacket);

    /**
     * Notification that service has successfully sent a packet to a client.
     * 
     * @param abstractPacket
     *            - a sent {@link AbstractPacket} packet.
     * @param senderIdentifier
     *            - a {@link String} sender id that the response is sent to.
     */
    void serverSent(AbstractPacket abstractPacket, String senderIdentifier);

    /**
     * Notification that server has received a request from the client.
     * 
     * @param abstractPacket
     *            - a received {@link AbstractPacket} packet.
     * @param senderIdentifier
     *            - a {@link String} sender id that has sent the request.
     */
    void serverReceived(AbstractPacket abstractPacket, String senderIdentifier);
}
