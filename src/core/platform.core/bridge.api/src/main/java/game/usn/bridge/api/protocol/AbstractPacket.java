/**
 * @file AbstractPacket.java
 * @brief AbstractPacket represents base protocol packet type for all protocol implementations.
 */

package game.usn.bridge.api.protocol;

import io.netty.buffer.ByteBuf;

/**
 * Abstract packet. Represents base protocol packet type for all protocol implementations. Upper layer data handlers can
 * implement special handling based on concrete data types as protocol handler will ensure concrete type instantiation.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractPacket
{
    // Errors, messages and args.
    private static final String ERROR_NO_IMPLEMENTED = "Concrete packet did not implement %s method!";
    private static final String ERROR_NO_IMPLEMENTED1 = "read";
    private static final String ERROR_NO_IMPLEMENTED2 = "write";

    /**
     * Attempt to read data from {@link ByteBuf} to concrete packet. If concrete packet does not override this method
     * throw new {@link UnsupportedOperationException}.
     * 
     * @param buf
     *            - source {@link ByteBuf}.
     */
    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException(String.format(ERROR_NO_IMPLEMENTED, ERROR_NO_IMPLEMENTED1));
    }

    /**
     * Attempt to write data from concrete packet to {@link ByteBuf}. If concrete packet does not override this method
     * throw new {@link UnsupportedOperationException}.
     * 
     * @param buf
     *            - destination {@link ByteBuf}.
     */
    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException(String.format(ERROR_NO_IMPLEMENTED, ERROR_NO_IMPLEMENTED2));
    }
}
