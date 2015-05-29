/**
 * @file USNPacketEncoder.java
 * @brief USN Packet encoder for converting internal consumer specific data objects to raw frame messages.
 */

package game.usn.bridge.pipeline.encoder;

import game.usn.bridge.api.protocol.AbstractPacket;
import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * USN Packet encoder. Pipeline will create a new instance of packet decoder for each connection. This encoder converts
 * consumer specific packets to outgoing {@link ByteBuf} messages if protocol provided supports them.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class USNPacketEncoder extends MessageToByteEncoder<AbstractPacket>
{

    // Consumer specific protocol instance. It defines in and out supported message types.
    private AbstractUSNProtocol consumerProtocol;

    /**
     * Ctor.
     * 
     * @param consumerProtocol
     *            - instance of {@link AbstractUSNProtocol} that is consumer specific.
     */
    public USNPacketEncoder(AbstractUSNProtocol consumerProtocol)
    {
        this.consumerProtocol = consumerProtocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractPacket msg, ByteBuf out) throws Exception
    {
        out.writeByte(this.consumerProtocol.getPacketId(msg.getClass()));
        msg.write(out);
    }
}
