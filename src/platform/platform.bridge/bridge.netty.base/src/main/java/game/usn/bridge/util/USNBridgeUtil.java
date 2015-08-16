/**
 * @file USNBridgeUtil.java
 * @brief Bridge utility functionality.
 */

package game.usn.bridge.util;

import game.usn.bridge.pipeline.ChannelOptions;

/**
 * Bridge utility functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class USNBridgeUtil
{

    /**
     * Channel options validator.
     * 
     * @param options
     *            - source {@link ChannelOptions} to validate.
     * @param isServerOptions
     *            - validation may vary between client and server specific options.
     * @throws IllegalArgumentException
     *             - throw {@link IllegalArgumentException} if channel options are not valid.
     */
    public static void validateChannelOptions(ChannelOptions options, boolean isServerOptions)
        throws IllegalArgumentException
    {
        // TODO: implement logic.
    }

}
