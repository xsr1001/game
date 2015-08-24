/**
 * @file AbstractPlatformProtocol.java
 * @brief Base protocol mapping.
 */

package platform.bridge.api.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.ws.ProtocolException;

/**
 * Abstract Platform protocol provides base protocol functionality and mapping for all consumer specific protocols.
 * Concrete protocol implementations should add their own specific packet mapping.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractPlatformProtocol
{
    // Errors, args, messages.
    private static final String ERROR_UNKNOWN_PACKET_CLASS = "Provided concrete packet class: [%s] has not been registered.";
    private static final String ERROR_UNKNOWN_PACKET_ID = "Provided concrete packet for id: [%d] has not been registered.";
    private static final String ERROR_INSTANTIATION = "Error instantiating packet with id: [%d].";
    private static final String ERROR_PACKET_ALREADY_REGISTERED = "Provided concrete packet class: [%s] has already been registered.";

    // Default number of bytes allocated for length header of the frame message.
    public static final int DEFAULT_FRAME_LENGTH_HEADER_SIZE = 2;

    // Packet to id and id to packet maps.
    private Map<Integer, Class<? extends AbstractPacket>> idToPacketMap;
    private Map<Class<? extends AbstractPacket>, Integer> packetToIdMap;

    // Number of ints allocated for length header of the frame message.
    private int frameLengthHeaderSize;

    /**
     * Ctor.
     */
    protected AbstractPlatformProtocol()
    {
        this(DEFAULT_FRAME_LENGTH_HEADER_SIZE);
    }

    /**
     * Ctor.
     * 
     * @param frameLengthHeaderSize
     *            - number of ints allocated for length header of the frame message.
     */
    protected AbstractPlatformProtocol(int frameLengthHeaderSize)
    {
        this.idToPacketMap = new HashMap<Integer, Class<? extends AbstractPacket>>();
        this.packetToIdMap = new HashMap<Class<? extends AbstractPacket>, Integer>();
        this.frameLengthHeaderSize = frameLengthHeaderSize;

        // Register base USN packets.
        registerBasePlatformPackets();
    }

    /**
     * Retrieve the protocol defined length of frame length header field.
     * 
     * @return
     */
    public int getFrameLengthHeaderSize()
    {
        return frameLengthHeaderSize;
    }

    /**
     * Register base USN packets that all consumer protocols should inherit.
     */
    private void registerBasePlatformPackets()
    {
        // TODO.
    }

    /**
     * Register a consumer defined packet.
     * 
     * @param packetId
     *            - a {@link int} unique packet id.
     * @param packetClass
     *            - a {@link Class} concrete type of {@link AbstractPacket} that defines a consumer defined packet.
     * @throws ProtocolException
     *             - throw {@link ProtocolException} if desired packet id already been taken.
     */
    protected final synchronized void registerPacket(int packetId, Class<? extends AbstractPacket> packetClass)
        throws ProtocolException
    {
        if (idToPacketMap.containsKey(packetId) || packetToIdMap.containsKey(packetClass))
        {
            throw new ProtocolException(String.format(ERROR_PACKET_ALREADY_REGISTERED, packetClass.getName()));
        }
        idToPacketMap.put(packetId, packetClass);
        packetToIdMap.put(packetClass, packetId);
    }

    /**
     * Retrieve current registered packet count.
     * 
     * @return - current amount of registered packets.
     */
    protected final int getCurrentRegisteredPacketCnt()
    {
        return idToPacketMap.size();
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
    public final AbstractPacket constructPacket(int packetId) throws ProtocolException
    {
        if (!idToPacketMap.containsKey(packetId))
        {
            throw new ProtocolException(String.format(ERROR_UNKNOWN_PACKET_ID, packetId));
        }

        try
        {
            return idToPacketMap.get(packetId).newInstance();
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
     *            - 1 int packet id.
     * @return - true if packet has been registered of false otherwise.
     */
    public final boolean packetRegistered(int packetId)
    {
        return idToPacketMap.containsKey(packetId);
    }

    /**
     * Check whether a packet with provided class name has been registered.
     * 
     * @param packetClass
     *            - a {@link Class} concrete type of {@link AbstractPacket} that defines a consumer defined packet.
     * @return - true if packet has been registered of false otherwise.
     */
    public final boolean packetRegistered(Class<? extends AbstractPacket> packetClass)
    {
        return packetToIdMap.containsKey(packetClass);
    }

    /**
     * Returns packet id for given concrete packet data type if registered.
     * 
     * @param packetClass
     *            - a {@link Class} concrete type of {@link AbstractPacket} that defines a consumer defined packet.
     * @return 1 int id for provided class type.
     * @throws ProtocolException
     *             - throws {@link ProtocolException} if packet has not yet been registered.
     */
    public final int getPacketId(Class<? extends AbstractPacket> packetClass) throws ProtocolException
    {
        if (!packetToIdMap.containsKey(packetClass))
        {
            throw new ProtocolException(String.format(ERROR_UNKNOWN_PACKET_CLASS, packetClass.getName()));
        }
        else
        {
            return packetToIdMap.get(packetClass);
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
        for (Entry<Integer, Class<? extends AbstractPacket>> entry : idToPacketMap.entrySet())
        {
            sb.append("[").append(entry.getKey()).append(" --> ").append(entry.getValue().getSimpleName()).append("]").append(
                System.lineSeparator());
        }
        sb.append("}");
        return sb.toString();
    }
}
