
package game.usn.bridge.pipeline.decoder;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.protocol.AbstractPacket;
import game.usn.bridge.api.protocol.IUSNProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class USNPacketDecoder extends MessageToMessageDecoder<ByteBuf>
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(USNPacketDecoder.class);

    // Args, errors, messages.
    private static final String WARN_UNKNOWN_MESSAGE = "Unknown message received with id: [%d] for protocol: [%s].";
    private static final String ERROR_PACKET_READ = "Failed to read all bytes for packet: [%s] from message with id: [%d] for protocol: [%s].";

    private IUSNProtocol consumerProtocol;

    public USNPacketDecoder(IUSNProtocol consumerProtocol)
    {
        this.consumerProtocol = consumerProtocol;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        short packetId = in.getShort(in.readerIndex());

        AbstractPacket packet = null;
        if (this.consumerProtocol.supportsPacket(packetId))
        {
            packet = this.consumerProtocol.createPacket(packetId);
            packet.read(in);

            if (in.readableBytes() != 0)
            {
                String errorMsg = String.format(ERROR_PACKET_READ, packet.getClass().getName(), packetId,
                    this.consumerProtocol);
                LOG.error(errorMsg);
                throw new BridgeException(errorMsg);
            }

            out.add(packet);
        }
        else
        {
            in.release();
            LOG.warn(String.format(WARN_UNKNOWN_MESSAGE, packetId, this.consumerProtocol));
        }
    }
}
