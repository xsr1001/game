/**
 * @file USNFrameInHandler.java
 * @brief <description>
 */

package game.usn.bridge.pipeline.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class USNFrameDecoder extends ByteToMessageDecoder
{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        // TODO Auto-generated method stub

    }

}
