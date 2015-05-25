/**
 * @file AbstractDataProxy.java
 * @brief <description>
 */

package game.usn.bridge.api.proxy;

import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class AbstractDataProxy extends ChannelInboundHandlerAdapter implements IDataProxy
{
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof AbstractDataProxy))
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        AbstractDataProxy dataProxy = AbstractDataProxy.class.cast(o);

        return getName().equals(dataProxy.getName());
    }

    @Override
    public int hashCode()
    {
        return getName().hashCode();
    }
}
