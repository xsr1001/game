
package game.usn.bridge.pipeline.decoder;

import game.usn.bridge.api.IUSNProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class USNPacketDecoder extends ByteToMessageDecoder
{
    public USNPacketDecoder(IUSNProtocol consumerProtocol)
    {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // TODO Auto-generated method stub

    }
}
