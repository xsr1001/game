/**
 * @file IConnectionObserver.java
 * @brief IConnectionObserver for client connection and disconnection events.
 */

package game.usn.bridge.api.listener;

/**
 * Observer for client connection and disconnection events. Should only be used in service context. There is no need to
 * provide actual proxy as a connection listener, this is intended for external observers only.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IConnectionObserver
{
    /**
     * Client connection state enumeration.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     */
    public enum EConnectionState
    {
        TRANSPORT_UP; // Initial client connection.
    }

    /**
     * Notify client connection observers.
     * 
     * @param USNEnpoint
     *            - a {@link String} USN end-point id.
     * @param state
     *            - a {@link EConnectionState} new state for given USN end-point.
     */
    void notifyConnectionState(String USNEnpoint, EConnectionState state);
}
