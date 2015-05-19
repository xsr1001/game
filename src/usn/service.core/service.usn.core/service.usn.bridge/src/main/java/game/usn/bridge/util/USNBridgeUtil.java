/**
 * @file USNBridgeUtil.java
 * @brief <description>
 */

package game.usn.bridge.util;

import java.net.ServerSocket;

public class USNBridgeUtil
{

    private static final String ERROR_NETWORK_PORT = "Invalid network port number: [%d]. Expected a value in [0, 65535].";
    private int getAvailablePort()
    {
        ServerSocket socket = null;
        try
        {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        }
        catch (Exception e)
        {
            return -1;
        }
        finally
        {
            if (socket != null)
            {
                socket.close();
            }
        }
    }

    public static void validateNetworkPort(short port) throws IllegalArgumentException
    {
        if (port < 0 || port > 65535)
        {
            throw new IllegalArgumentException(String.format(ERROR_NETWORK_PORT, port));
        }
    }

}
