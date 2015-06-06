/**
 * @file IConnectionListener.java
 * @brief IConnectionListener for client connection and disconnection events.
 */

package game.usn.bridge.api.listener;

/**
 * Listener for client connection and disconnection events. Should only be used on service context. There is no need to
 * provide actual proxy as a connection listener, this is intended for external listeners only.
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
        TRANSPORT_UP, STAND_BY, CONNECTED, OFFLINE;
    }

    /**
     * Notify channel event to listeners. Only used at service channel to notify new client connection state.
     * 
     * @param USNEnpoint
     *            - a {@link String} USN end-point id. This is actually a {@link Channel#toString} output that
     *            concatenates channel id with remote and local addresses.
     * @param state
     *            - a {@link EConnectionState} new state for given USN end-point.
     */
    void notifyConnectionState(String USNEnpoint, EConnectionState state);
}
