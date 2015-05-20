/**
 * @file USNBridgeUtil.java
 * @brief Bridge utility functionality.
 */

package game.usn.bridge.util;

/**
 * Bridge utility functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class USNBridgeUtil
{
    // Args, msgs, errors.
    private static final String ERROR_NETWORK_PORT = "Invalid network port number: [%d]. Expected a value in [0, 65535].";

    /**
     * Validate provided network port number.
     * 
     * @param port
     *            - provided port number.
     * @throws IllegalArgumentException
     *             - throw {@link IllegalArgumentException} if port number not valid.
     */
    public static void validateNetworkPort(short port) throws IllegalArgumentException
    {
        if (port < 0 || port > 65535)
        {
            throw new IllegalArgumentException(String.format(ERROR_NETWORK_PORT, port));
        }
    }

}
