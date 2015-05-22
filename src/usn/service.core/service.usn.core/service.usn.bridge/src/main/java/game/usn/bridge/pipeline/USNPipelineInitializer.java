/**
 * @file USNPipelineProvider.java
 * @brief <description>
 */

package game.usn.bridge.pipeline;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.IUSNProtocol;
import game.usn.bridge.api.listener.IChannelListener;
import game.usn.bridge.api.listener.IConnectionListener;
import game.usn.bridge.api.listener.IConnectionListener.EConnectionState;
import game.usn.bridge.api.proxy.AbstractDataProxy;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class USNPipelineInitializer extends ChannelInitializer<Channel>
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(USNPipelineInitializer.class);

    // Args, messages, errors.
    private static final String MSG_NEW_CONNECTION_FORMAT = "New %s with host: [%s].";
    private static final String MSG_NEW_CONNECTION1 = "client connection accepted";
    private static final String MSG_NEW_CONNECTION2 = "remote connection established";
    private static final String ERROR_NO_CONNECTION_OPTIONS = "Cannot retrieve connection options from channel";

    // Server listeners key that gets attached to the server socket channel.
    private static final String SERVER_LISTENERS_KEY = "serverListenerKey";
    private static final String CONNECTION_OPTIONS_KEY = "connectionOptions";
    public static final AttributeKey<Set<IChannelListener>> SERVER_LISTENER_ATR_KEY = AttributeKey.newInstance(SERVER_LISTENERS_KEY);
    public static final AttributeKey<ConnectionOptions> CONNECTION_OPTIONS_ATR_KEY = AttributeKey.newInstance(CONNECTION_OPTIONS_KEY);

    // Handler names.
    private static final String HANDLER_TIMEOUT = "handler_timeout";
    private static final String HANDLER_CONSUMER_FORMAT = "handler_consumer_%d";
    private static final String HANDLER_PROXY = "handler_proxy";

    ChannelHandler protocolDecoder;
    List<ChannelHandler> consumerHandlerList;
    ChannelInboundHandlerAdapter consumerProxy;

    int consumerHandlerCtr = 0;

    public USNPipelineInitializer(IUSNProtocol protocol, List<ChannelHandler> handlerStack, AbstractDataProxy dataProxy)
    {
        this.consumerHandlerList = new LinkedList<ChannelHandler>(handlerStack);
        this.consumerProxy = dataProxy;
    }

    /**
     * {@inheritDoc} This method will attempt to initialized USN and consumer specified network stack for incoming and
     * outgoing data.
     */
    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        LOG.enterMethod();

        ConnectionOptions connectionOptions = ch.attr(CONNECTION_OPTIONS_ATR_KEY).get();
        if (connectionOptions == null)
        {
            throw new BridgeException(ERROR_NO_CONNECTION_OPTIONS);
        }

        // Initialize base USN pipeline with non modifiable handler chain.
        initBaseUSNPipeline(ch, connectionOptions);

        for (ChannelHandler handler : consumerHandlerList)
        {
            ch.pipeline().addLast(String.format(HANDLER_CONSUMER_FORMAT, ++consumerHandlerCtr), handler);
        }

        ch.pipeline().addLast(HANDLER_PROXY, consumerProxy);

    }

    private void initBaseUSNPipeline(Channel ch, ConnectionOptions options)
    {
        LOG.enterMethod();

        String connectionAddress = ((InetSocketAddress) ch.remoteAddress()).getAddress().toString();
        LOG.info(String.format(MSG_NEW_CONNECTION_FORMAT, options.isServer() ? MSG_NEW_CONNECTION1
            : MSG_NEW_CONNECTION2, connectionAddress));

        // Notify connection listeners if any.
        for (IConnectionListener listener : options.getConnectionListenerSet())
        {
            listener.notifyConnectionState(connectionAddress, EConnectionState.STAND_BY);
        }

        if (options.isEnableReadTimeoutHandler())
        {
            ch.pipeline().addLast(HANDLER_TIMEOUT,
                new ReadTimeoutHandler(options.getReadTimeOutChannelExpirationSec(), TimeUnit.SECONDS));
        }

        // Add byte to message decoder.

        // Add message to packet decoder.

        if (isSecure)
        {
            // Add SSL decoder. Maybe put this higher in the chain, check netty supported SSL mechanisms.
        }

        // Enable mandatory service info???

        LOG.exitMethod();
    }
}
