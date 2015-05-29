/**
 * @file AbstractUSNProtocol.java
 * @brief Base protocol mapping.
 */

package game.usn.bridge.api.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.ws.ProtocolException;

/**
 * Abstract USN protocol provides base protocol functionality and mapping for all consumer specific protocols. Concrete
 * protocol implementations should add their own specific packet mapping. Currently protocol supports 256 different
 * packet types per consumer (some of which are base).
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractUSNProtocol
{
    // Errors, args, messages.
    private static final String ERROR_UNKNOWN_PACKET_CLASS = "Provided concrete packet class: [%s] has not been registered.";
    private static final String ERROR_UNKNOWN_PACKET_ID = "Provided concrete packet for id: [%d] has not been registered.";
    private static final String ERROR_INSTANTIATION = "Error instantiating packet with id: [%d].";
    private static final String ERROR_PACKET_ALREADY_REGISTERED = "Provided concrete packet class: [%s] has already been registered.";

    // Default number of bytes allocated for length header of the frame message.
    private static final int DEFAULT_FRAME_LENGTH_HEADER_SIZE = 2;

    // Packet to id and id to packet maps.
    private Map<Byte, Class<? extends AbstractPacket>> idToPacketMap;
    private Map<Class<? extends AbstractPacket>, Byte> packetToIdMap;

    // Number of bytes allocated for length header of the frame message.
    private int frameLengthHeaderSize;

    /**
     * Ctor.
     */
    protected AbstractUSNProtocol()
    {
        this(DEFAULT_FRAME_LENGTH_HEADER_SIZE);
    }

    /**
     * Ctor.
     * 
     * @param frameLengthHeaderSize
     *            - number of bytes allocated for length header of the frame message.
     */
    protected AbstractUSNProtocol(int frameLengthHeaderSize)
    {
        this.idToPacketMap = new HashMap<Byte, Class<? extends AbstractPacket>>();
        this.packetToIdMap = new HashMap<Class<? extends AbstractPacket>, Byte>();
        this.frameLengthHeaderSize = frameLengthHeaderSize;

        // Register base USN packets.
        registerBaseUSNPackets();
    }

    /**
     * Retrieve the protocol defined length of frame length header field.
     * 
     * @return
     */
    public int getFrameLengthHeaderSize()
    {
        return this.frameLengthHeaderSize;
    }

    /**
     * Register base USN packets that all consumer protocols should inherit.
     */
    private void registerBaseUSNPackets()
    {

    }

    /**
     * Register a consumer defined packet.
     * 
     * @param packetId
     *            - a {@link Byte} unique packet id.
     * @param packetClass
     *            - a {@link Class} concrete type of {@link AbstractPacket} that defines a consumer defined packet.
     * @throws ProtocolException
     *             - throw {@link ProtocolException} if desired packet id already been taken.
     */
    protected final synchronized void registerPacket(Byte packetId, Class<? extends AbstractPacket> packetClass)
        throws ProtocolException
    {
        if (this.idToPacketMap.containsKey(packetId) || this.packetToIdMap.containsKey(packetClass))
        {
            throw new ProtocolException(String.format(ERROR_PACKET_ALREADY_REGISTERED, packetClass.getName()));
        }
        this.idToPacketMap.put(packetId, packetClass);
    }

    /**
     * Retrieve current registered packet count.
     * 
     * @return - current amount of registered packets.
     */
    protected final int getCurrentRegisteredPacketCnt()
    {
        return this.idToPacketMap.size();
    }

    /**
     * Constructs a new instance of packet with provided packet id.
     * 
     * @param packetId
     *            - unique id on the packet to create.
     * @return new concrete instance of {@link AbstractPacket} if registered for given packet id.
     * @throws ProtocolException
     *             - throws {@link ProtocolException} on invalid packet id or instantiation exception.
     */
    public final AbstractPacket constructPacket(byte packetId) throws ProtocolException
    {
        if (!this.idToPacketMap.containsKey(packetId))
        {
            throw new ProtocolException(String.format(ERROR_UNKNOWN_PACKET_ID, packetId));
        }

        try
        {
            return this.idToPacketMap.get(packetId).newInstance();
        }
        catch (IllegalAccessException iae)
        {
            throw new ProtocolException(String.format(ERROR_INSTANTIATION, packetId), iae);
        }
        catch (InstantiationException ie)
        {
            throw new ProtocolException(String.format(ERROR_INSTANTIATION, packetId), ie);
        }
    }

    /**
     * Check whether a packet with provided packet id has been registered.
     * 
     * @param packetId
     *            - 1 byte packet id.
     * @return - true if packet has been registered of false otherwise.
     */
    public final boolean packetRegistered(Byte packetId)
    {
        return this.idToPacketMap.containsKey(packetId);
    }

    /**
     * Returns packet id for given concrete packet data type if registered.
     * 
     * @param packetClass
     *            - a {@link Class} concrete type of {@link AbstractPacket} that defines a consumer defined packet.
     * @return 1 byte id for provided class type.
     * @throws ProtocolException
     *             - throws {@link ProtocolException} if packet has not yet been registered.
     */
    public final byte getPacketId(Class<? extends AbstractPacket> packetClass) throws ProtocolException
    {
        if (!this.packetToIdMap.containsKey(packetClass))
        {
            throw new ProtocolException(String.format(ERROR_UNKNOWN_PACKET_CLASS, packetClass.getName()));
        }
        else
        {
            return this.packetToIdMap.get(packetClass);
        }
    }

    /**
     * Returns basic protocol mapping.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Protocol mapping: {");
        for (Entry<Byte, Class<? extends AbstractPacket>> entry : this.idToPacketMap.entrySet())
        {
            sb.append("[").append(entry.getKey()).append(" --> ").append(entry.getValue().getSimpleName()).append("]").append(
                System.lineSeparator());
        }
        sb.append("}");
        return sb.toString();
    }
}
