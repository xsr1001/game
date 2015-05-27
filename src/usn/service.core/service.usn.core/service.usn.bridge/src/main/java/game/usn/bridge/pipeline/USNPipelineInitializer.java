/**
 * @file USNPipelineInitializer.java
 * @brief USNPipelineInitializer provides network stack initialization logic for new connections.
 */

package game.usn.bridge.pipeline;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;
import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.listener.IChannelListener;
import game.usn.bridge.api.listener.IConnectionListener;
import game.usn.bridge.api.listener.IConnectionListener.EConnectionState;
import game.usn.bridge.api.proxy.AbstractDataProxy;
import game.usn.bridge.pipeline.decoder.USNPacketDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * USNPipelineInitializer. Provides network stack initialization logic for new connections. Consumer must provide valid
 * parameters for initializing base USN network stack and may provide additional consumer specific data handlers.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class USNPipelineInitializer extends ChannelInitializer<Channel>
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(USNPipelineInitializer.class);

    // Args, messages, errors.
    private static final String MSG_NEW_CONNECTION_FORMAT = "New %s with host: [%s].";
    private static final String MSG_NEW_CONNECTION1 = "client connection accepted";
    private static final String MSG_NEW_CONNECTION2 = "remote connection established";
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";
    private static final String ARG_CONSUMER_PROXY = "consumerProxy";
    private static final String ARG_CONSUMER_PROTOCOL = "consumerProtocol";
    private static final String ERROR_NO_CHANNEL_OPTIONS = "Cannot retrieve channel options attribute from channel.";

    // Channel attribute keys.
    private static final String CHANNEL_LISTENERS_KEY = "channelListenerKey";
    private static final String CHANNEL_OPTIONS_KEY = "channelOptionsKey";
    public static final AttributeKey<Set<IChannelListener>> CHANNEL_LISTENER_ATR_KEY = AttributeKey.newInstance(CHANNEL_LISTENERS_KEY);
    public static final AttributeKey<ChannelOptions> CHANNEL_OPTIONS_ATR_KEY = AttributeKey.newInstance(CHANNEL_OPTIONS_KEY);

    // Handler names.
    private static final String HANDLER_TIMEOUT = "handler_timeout";
    private static final String HANDLER_FRAME_DECODER = "handler_frame_decoder";
    private static final String HANDLER_FRAME_ENCODER = "handler_frame_encoder";
    private static final String HANDLER_PACKET_DECODER = "handler_packet_decoder";
    private static final String HANDLER_CONSUMER_DECODER = "handler_consumer_decoder_%s";
    private static final String HANDLER_PROXY = "handler_proxy";

    // In/Out packet data end-point.
    AbstractDataProxy consumerProxy;

    /**
     * Ctor.
     * 
     * @param consumerProxy
     *            - an implementation of {@link AbstractDataProxy} that will define actual consumer end-point. All
     *            incoming packets will be routed to it. Proxy should contain consumer specific protocol object and
     *            optionally additional pipeline in and out handlers.
     */
    public USNPipelineInitializer(AbstractDataProxy consumerProxy)
    {
        ArgsChecker.errorOnNull(consumerProxy, ARG_CONSUMER_PROXY);
        ArgsChecker.errorOnNull(consumerProxy.getProtocol(), ARG_CONSUMER_PROTOCOL);

        this.consumerProxy = consumerProxy;
    }

    /**
     * This method will attempt to initialize USN and consumer specified network pipeline stack for incoming and
     * outgoing data.
     */
    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        LOG.enterMethod();

        ChannelOptions options = ch.attr(CHANNEL_OPTIONS_ATR_KEY).get();
        if (options == null)
        {
            throw new BridgeException(ERROR_NO_CHANNEL_OPTIONS);
        }

        String hostAddress = ((InetSocketAddress) ch.remoteAddress()).getAddress().toString();
        LOG.info(String.format(MSG_NEW_CONNECTION_FORMAT, options.isServer() ? MSG_NEW_CONNECTION1
            : MSG_NEW_CONNECTION2, hostAddress));

        // Notify server connection listeners if any.
        if (options.isServer() && options.getConnectionListenerSet() != null)
        {
            for (IConnectionListener listener : options.getConnectionListenerSet())
            {
                listener.notifyConnectionState(ch.toString(), EConnectionState.TRANSPORT_UP);
            }
        }

        // Initialize base USN pipeline with non consumer modifiable handler chain.
        initBaseUSNPipeline(ch, options);

        // Add additional consumer specific in handlers.
        if (this.consumerProxy.getInHandlerList() != null)
        {
            for (ChannelHandler handler : this.consumerProxy.getInHandlerList())
            {
                ch.pipeline().addLast(String.format(HANDLER_CONSUMER_DECODER, handler.getClass().getSimpleName()),
                    handler);
            }
        }

        // TODO: add out handlers.

        // Add actual data consumer end-point.
        ch.pipeline().addLast(HANDLER_PROXY, this.consumerProxy);

        LOG.exitMethod();
    }

    /**
     * Initialize base USN network pipeline. This consists of low level connection handlers as well as basic data
     * decoders and encoders. User defined data handlers are applied higher in the pipeline. USN pipeline: ([timeout
     * handler]) --> [frame decoder] --> [packet decoder] --> ([SSL handler]) --> ([ServiceInfo handler])
     * 
     * @param ch
     *            - a {@link Channel} instance to apply pipeline to.
     * @param options
     *            - a {@link ChannelOptions} defining consumer specific configuration for given channel.
     */
    private void initBaseUSNPipeline(Channel ch, ChannelOptions options)
    {
        LOG.enterMethod(ARG_CHANNEL_OPTIONS, options);

        // Enable read timeout handler for incoming connections.
        if (options.isServer() && options.isEnableReadTimeoutHandler())
        {
            ch.pipeline().addLast(HANDLER_TIMEOUT,
                new ReadTimeoutHandler(options.getReadTimeOutChannelExpirationSec(), TimeUnit.SECONDS));
        }

        // TODO: add write timeout handler for client connections.

        // Add frame decoder and encoder.
        ch.pipeline().addLast(
            HANDLER_FRAME_DECODER,
            new LengthFieldBasedFrameDecoder(1024, 0, this.consumerProxy.getProtocol().getFrameSize(), 0,
                this.consumerProxy.getProtocol().getFrameSize()));
        ch.pipeline().addLast(HANDLER_FRAME_ENCODER,
            new LengthFieldPrepender(this.consumerProxy.getProtocol().getFrameSize(), 0));

        // Add packet decoder.
        ch.pipeline().addLast(HANDLER_PACKET_DECODER, new USNPacketDecoder(this.consumerProxy.getProtocol()));

        if (options.isSSLEnabled())
        {
            // TODO: Add SSL decoder. Maybe put this higher in the chain, check Netty supported SSL mechanisms.
        }

        // TODO: Enable mandatory service info handler???

        LOG.exitMethod();
    }

    /**
     * Output some basic data about this initializer.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append(" for proxy: [").append(this.consumerProxy).append("] ");
        return sb.toString();
    }
}
