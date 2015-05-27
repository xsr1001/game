/**
 * @file AbstractPacket.java
 * @brief <description>
 */

package game.usn.bridge.api.protocol;

import io.netty.buffer.ByteBuf;

public abstract class AbstractPacket
{
    protected static final String ERROR_NO_IMPLEMENTED = "Concrete packet must implement %s method!";
    protected static final String ERROR_NO_IMPLEMENTED1 = "read";

    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException(String.format(ERROR_NO_IMPLEMENTED, ERROR_NO_IMPLEMENTED1));
    }
}
