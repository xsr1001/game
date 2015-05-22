/**
 * @file ConnectionOptions.java
 * @brief <description>
 */

package game.usn.bridge.pipeline;

import game.usn.bridge.api.listener.IConnectionListener;

import java.util.Set;

public class ConnectionOptions
{
    private int readTimeOutChannelExpirationSec;
    private boolean SSLEnabled;
    private boolean server;

    Set<IConnectionListener> connectionListenerSet;

    public boolean isSSLEnabled()
    {
        return SSLEnabled;
    }

    public void setSSLEnabled(boolean sSLEnabled)
    {
        SSLEnabled = sSLEnabled;
    }

    public int getReadTimeOutChannelExpirationSec()
    {
        return readTimeOutChannelExpirationSec;
    }

    public void setReadTimeOutChannelExpirationSec(int readTimeOutChannelExpirationSec)
    {
        this.readTimeOutChannelExpirationSec = readTimeOutChannelExpirationSec;
    }

    public boolean isEnableReadTimeoutHandler()
    {
        return this.readTimeOutChannelExpirationSec != -1;
    }

    public boolean isServer()
    {
        return server;
    }

    public void setServer(boolean server)
    {
        this.server = server;
    }

    public Set<IConnectionListener> getConnectionListenerSet()
    {
        return connectionListenerSet;
    }

    public void setConnectionListenerSet(Set<IConnectionListener> connectionListenerSet)
    {
        this.connectionListenerSet = connectionListenerSet;
    }

}
