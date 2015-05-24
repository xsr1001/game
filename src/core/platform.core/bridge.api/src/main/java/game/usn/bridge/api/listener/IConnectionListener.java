/**
 * @file IConnectionListener.java
 * @brief <description>
 */

package game.usn.bridge.api.listener;

public interface IConnectionListener
{
    public enum EConnectionState
    {
        TRANSPORT_UP, STAND_BY, CONNECTED, OFFLINE;
    }

    void notifyConnectionState(String USNEnpoint, EConnectionState state);
}
