/**
 * @file PlatformBridgeUtil.java
 * @brief Bridge utility functionality.
 */

package platform.bridge.base.util;

import platform.bridge.api.proxy.BridgeOptions;

/**
 * Bridge utility functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class PlatformBridgeUtil
{

    /**
     * Channel options validator.
     * 
     * @param options
     *            - source {@link BridgeOptions} to validate.
     * @param isServerOptions
     *            - validation may vary between client and server specific options.
     * @throws IllegalArgumentException
     *             - throw {@link IllegalArgumentException} if channel options are not valid.
     */
    public static void validateChannelOptions(BridgeOptions options, boolean isServerOptions)
        throws IllegalArgumentException
    {
        // TODO: implement logic.
    }
}
