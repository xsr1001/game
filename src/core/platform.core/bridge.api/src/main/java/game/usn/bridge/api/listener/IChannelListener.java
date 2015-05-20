/**
 * @file IServerListener.java
 * @brief <description>
 */

package game.usn.bridge.api.listener;

public interface IChannelListener
{
    public void notifyChannelUp();

    public void notifyChannelDown();

    public void notifyChannelError();
}
