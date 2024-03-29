/**
 * @file PlatformPacketDecoder.java
 * @brief Platform Packet decoder for converting raw frame messages to internal consumer specific data objects.
 */

package platform.bridge.base.pipeline.decoder;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import javax.xml.ws.ProtocolException;

import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;

/**
 * Platform Packet decoder. Pipeline will create a new instance of packet decoder for each connection. This decoder
 * converts incoming messages to consumer specific packets if protocol provided supports them.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformPacketDecoder extends MessageToMessageDecoder<ByteBuf>
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(PlatformPacketDecoder.class);

    // Args, errors, messages.
    private static final String WARN_UNKNOWN_MESSAGE = "Unknown message received with id: [%d] for protocol: [%s].";
    private static final String ERROR_PACKET_READ = "Failed to read all bytes for packet: [%s] from message with id: [%d] for protocol: [%s].";
    private static final String ARG_CONSUMER_PROTOCOL = "consumerProtocol";

    // Consumer specific protocol instance. It defines in and out supported message types.
    private AbstractPlatformProtocol consumerProtocol;

    /**
     * Ctor.
     * 
     * @param consumerProtocol
     *            - instance of {@link AbstractPlatformProtocol} that is consumer specific.
     */
    public PlatformPacketDecoder(AbstractPlatformProtocol consumerProtocol)
    {
        ArgsChecker.errorOnNull(consumerProtocol, ARG_CONSUMER_PROTOCOL);
        this.consumerProtocol = consumerProtocol;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // Get message id.
        int messageId = AbstractPacket.readInt(in);

        // Create and populate concrete packet if available.
        AbstractPacket packet = null;
        if (consumerProtocol.packetRegistered(messageId))
        {
            packet = consumerProtocol.constructPacket(messageId);
            packet.read(in);

            // Check if packet has not consumed all the bytes in bytebuf.
            if (in.readableBytes() != 0)
            {
                LOG.error(String.format(ERROR_PACKET_READ, packet.getClass().getName(), messageId, consumerProtocol));
                throw new ProtocolException(String.format(ERROR_PACKET_READ, packet.getClass().getName(), messageId,
                    consumerProtocol));
            }

            // Otherwise add packet to the upstream consumer specific handler.
            out.add(packet);
        }
        else
        {
            LOG.warn(String.format(WARN_UNKNOWN_MESSAGE, messageId, consumerProtocol));
            throw new ProtocolException(String.format(WARN_UNKNOWN_MESSAGE, messageId, consumerProtocol));
        }
    }
}
