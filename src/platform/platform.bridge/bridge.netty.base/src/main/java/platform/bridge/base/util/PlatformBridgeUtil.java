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
    private static final String ERROR_INVALID_BRIDGE_OPTIONS = "Validation of provided bridge options failed.";

    /**
     * Bridge options validator.
     * 
     * @param options
     *            - source {@link BridgeOptions} to validate.
     * @param isServerOptions
     *            - validation may vary between client and server specific options. If null, validate for both.
     * @throws IllegalArgumentException
     *             - throw {@link IllegalArgumentException} if bridge options are not valid.
     */
    public static void validateBridgeOptions(BridgeOptions options, Boolean isServerOptions)
        throws IllegalArgumentException
    {
        if (options == null || options.get(BridgeOptions.KEY_IS_SERVER) == null)
        {
            throw new IllegalArgumentException(ERROR_INVALID_BRIDGE_OPTIONS);
        }
    }
}
