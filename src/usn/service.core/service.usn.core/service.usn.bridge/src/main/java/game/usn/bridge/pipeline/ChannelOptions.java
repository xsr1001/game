/**
 * @file ChannelOptions.java
 * @brief ChannelOptions provides basic channel and connection options.
 */

package game.usn.bridge.pipeline;

import game.usn.bridge.api.listener.IConnectionListener;
import game.usn.bridge.util.USNBridgeUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * ChannelOptions. Provides basic channel and connection options. This is just a container for various options, it does
 * not attempt to validate any data. Refer to {@link USNBridgeUtil#validateChannelOptions(ChannelOptions, boolean)} for
 * options validation.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ChannelOptions
{
    // Read timeout in seconds. Server option only.
    private int readTimeOutChannelExpirationSec = -1;

    // Enable or disable SSL.
    private boolean SSLEnabled;

    // Determines if options are server or client specific.
    private boolean server;

    // Listeners for client connection event. Server option only.
    Set<IConnectionListener> connectionListenerSet;

    /**
     * No arg ctor.
     */
    public ChannelOptions()
    {

    }

    /**
     * Ctor.
     * 
     * @param isSSLEnabled
     *            - enables SSL encryption between USN end-points.
     * @param readTimeoutSeconds
     *            - set read timeout amount of seconds.
     * @param server
     *            - determines whether these options are server specific or client specific.
     * @param externalConnectionListenerSet
     *            - a {@link Set}<{@link IConnectionListener}> of connection listeners. Provides a way to notify
     *            external listeners about client connection events.
     */
    public ChannelOptions(boolean isSSLEnabled, int readTimeoutSeconds, boolean server,
        Set<IConnectionListener> externalConnectionListenerSet)
    {
        this.SSLEnabled = isSSLEnabled;
        this.server = server;
        this.connectionListenerSet = new HashSet<IConnectionListener>(externalConnectionListenerSet);
        this.readTimeOutChannelExpirationSec = readTimeoutSeconds;
    }

    // Getters and setters.

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

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append(":").append(System.lineSeparator());
        sb.append("server").append("-->").append(server).append(System.lineSeparator());
        sb.append("SSLEnabled").append("-->").append(SSLEnabled).append(System.lineSeparator());
        sb.append("readTimeOutChannelExpirationSec").append("-->").append(readTimeOutChannelExpirationSec).append(
            System.lineSeparator());
        sb.append("connectionListenerSet").append("-->").append(connectionListenerSet).append(System.lineSeparator());

        return sb.toString();
    }
}
