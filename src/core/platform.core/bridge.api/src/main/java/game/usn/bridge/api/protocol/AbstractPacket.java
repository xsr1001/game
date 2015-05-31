/**
 * @file AbstractPacket.java
 * @brief AbstractPacket represents base protocol packet type for all protocol implementations.
 */

package game.usn.bridge.api.protocol;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;

import javax.xml.ws.ProtocolException;

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
    private static final String ERROR_ENCODING = "Cannot retrieve %s charset bytes.";
    private static final String ARG_DEFAULT_STRING_ENCODING = "UTF-8";
    private static final String ERROR_VARINT_READ = "Cannot read variable int from buffer because it is too large.";

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

    /**
     * 
     * @param buf
     *            - source {@link ByteBuf} to read from.
     * @return - read {@link String}.
     * @throws ProtocolException
     *             - throw {@link ProtocolException} on encoding error.
     */
    protected final String readString(ByteBuf buf) throws ProtocolException
    {
        try
        {
            int numBytes = readInt(buf);
            byte[] bytes = new byte[numBytes];
            buf.readBytes(bytes);
            return new String(bytes, ARG_DEFAULT_STRING_ENCODING);
        }
        catch (UnsupportedEncodingException uee)
        {
            throw new ProtocolException(String.format(ERROR_ENCODING, ARG_DEFAULT_STRING_ENCODING), uee);
        }
    }

    /**
     * Attempts to write provided {@link String} to destination {@link ByteBuf}.
     * 
     * @param buf
     *            - destination {@link ByteBuf} to write to.
     * @param string
     *            - source {@link String}.
     * @throws ProtocolException
     *             - throw {@link ProtocolException} on encoding error.
     */
    protected final void writeString(ByteBuf buf, String string) throws ProtocolException
    {
        try
        {
            byte[] byteBuf = string.getBytes(ARG_DEFAULT_STRING_ENCODING);
            writeInt(byteBuf.length, buf);
            buf.writeBytes(byteBuf);
        }
        catch (UnsupportedEncodingException uee)
        {
            throw new ProtocolException(String.format(ERROR_ENCODING, ARG_DEFAULT_STRING_ENCODING), uee);
        }
    }

    /**
     * Read variable size int.
     * 
     * @param input
     *            - source {@link ByteBuf}.
     * @return - read integer.
     * @throws ProtocolException
     *             - throw {@link ProtocolException} on read error.
     */
    public static final int readInt(ByteBuf input) throws ProtocolException
    {
        return readInt(input, 5);
    }

    /**
     * Read variable size int.
     * 
     * @param input
     *            - source {@link ByteBuf}.
     * @param maxBytes
     *            - maximum size of the integer.
     * @return - read integer.
     * @throws ProtocolException
     *             - throw {@link ProtocolException} on read error.
     */
    public static final int readInt(ByteBuf input, int maxBytes) throws ProtocolException
    {
        int out = 0;
        int bytes = 0;
        byte in;
        while (true)
        {
            in = input.readByte();

            out |= (in & 0x7F) << (bytes++ * 7);

            if (bytes > maxBytes)
            {
                throw new ProtocolException(ERROR_VARINT_READ);
            }

            if ((in & 0x80) != 0x80)
            {
                break;
            }
        }

        return out;
    }

    /**
     * Write variable int.
     * 
     * @param value
     *            - variable int to write.
     * @param output
     *            - destination {@link ByteBuf} to write to.
     */
    public static final void writeInt(int value, ByteBuf output)
    {
        int part;
        while (true)
        {
            part = value & 0x7F;

            value >>>= 7;
            if (value != 0)
            {
                part |= 0x80;
            }

            output.writeByte(part);

            if (value == 0)
            {
                break;
            }
        }
    }
}
