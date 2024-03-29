/**
 * @file PlatformPacketEncoder.java
 * @brief Platform Packet encoder for converting internal consumer specific data objects to raw frame messages.
 */

package platform.bridge.base.pipeline.encoder;

import game.core.util.ArgsChecker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.xml.ws.ProtocolException;

import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.protocol.AbstractPlatformProtocol;

/**
 * Platform Packet encoder. Pipeline will create a new instance of packet decoder for each connection. This encoder
 * converts consumer specific packets to outgoing {@link ByteBuf} messages if protocol provided supports them.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class PlatformPacketEncoder extends MessageToByteEncoder<AbstractPacket>
{
    // Errors, args, messages.
    private static final String WARN_UNKNOWN_MESSAGE = "Sending unknown message with class: [%s] for protocol: [%s].";
    private static final String ARG_CONSUMER_PROTOCOL = "consumerProtocol";

    // Consumer specific protocol instance. It defines in and out supported message types.
    private AbstractPlatformProtocol consumerProtocol;

    /**
     * Ctor.
     * 
     * @param consumerProtocol
     *            - instance of {@link AbstractPlatformProtocol} that is consumer specific.
     */
    public PlatformPacketEncoder(AbstractPlatformProtocol consumerProtocol)
    {
        ArgsChecker.errorOnNull(consumerProtocol, ARG_CONSUMER_PROTOCOL);
        this.consumerProtocol = consumerProtocol;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, AbstractPacket msg, ByteBuf out) throws Exception
    {
        if (!consumerProtocol.packetRegistered(msg.getClass()))
        {
            throw new ProtocolException(String.format(WARN_UNKNOWN_MESSAGE, msg.getClass().getName(),
                this.consumerProtocol));
        }
        AbstractPacket.writeInt(consumerProtocol.getPacketId(msg.getClass()), out);
        msg.write(out);
    }
}
