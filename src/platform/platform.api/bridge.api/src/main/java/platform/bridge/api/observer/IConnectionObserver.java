/**
 * @file IConnectionObserver.java
 * @brief IConnectionObserver is an observer for client life-cycle events.
 */

package platform.bridge.api.observer;

/**
 * Observer for client life-cycle events. Will only be used in service context. Bridge layer will notify observers with
 * client life-cycle change events.
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
        ONLINE, // Initial client connection - physical connection
        TRANSPORT_UP, // Platform stack validated, client is fully connected.
        TRANSPORT_DOWN // Client has disconnected.
    }

    /**
     * Notify client connection observers.
     * 
     * @param clientChannelId
     *            - a {@link String} unique client channel id.
     * @param state
     *            - a {@link EConnectionState} new connection state of a client connection.
     */
    void notifyConnectionState(String clientChannelId, EConnectionState state);
}
