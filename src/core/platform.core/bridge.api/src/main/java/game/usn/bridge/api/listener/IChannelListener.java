/**
 * @file IServerListener.java
 * @brief <description>
 */

package game.usn.bridge.api.listener;

public interface IChannelListener
{
    public void notifyChannelUp(String proxyName);

    public void notifyChannelDown(String proxyName);

    public void notifyChannelError(String proxyName);
}
