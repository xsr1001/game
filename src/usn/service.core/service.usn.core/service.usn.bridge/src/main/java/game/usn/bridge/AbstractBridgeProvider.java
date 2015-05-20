/**
 * @file AbstractBridgeProvider.java
 * @brief AbstractBridgeProvider attempts to provide base functionality for unified service network.
 */

package game.usn.bridge;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;
import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.listener.IChannelListener;
import game.usn.bridge.pipeline.USNPipelineInitializer;
import game.usn.bridge.util.USNBridgeUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.internal.PlatformDependent;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract bridge provider provides base functionality for providing unified service network bridge functionality for
 * various consumers. It provides functionality for initializing consumer specific protocol stacks.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractBridgeProvider
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBridgeProvider.class);

    // Errors, args, messages.
    private static final String ARG_WORKER_GROUP = "workerGroup";
    private static final String ARG_PIPELINE_INITIALIZER = "pipelineInitializer";
    private static final String ARG_SERVICE_PORT = "servicePort";
    private static final String ERROR_SOCKET_BIND = "Error binding server socket.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_UNKNOWN_HOST = "Cannot retrieve hostname for socket bind.";
    private static final String ERROR_GENERAL_EXCEPTION = "General exception thrown.";
    private static final String ERROR_NPE = "Null pointer exception thrown.";
    private static final String ERROR_CHANNEL_EXCEPTION = "Channel exception thrown.";

    // Server listeners key that gets attached to the server socket channel.
    private static final String LISTENERS_KEY = "serverListenerKey";
    public static final AttributeKey<Set<IChannelListener>> LISTENER_ATR_KEY = AttributeKey.newInstance(LISTENERS_KEY);

    // Default socket linger duration in seconds.
    // TODO: configuration
    private static final int DEFAULT_SOCKET_LINGER_SEC = 3;

    // Default worker group size.
    // TODO: configuration
    private static final int DEFAULT_WORKER_GROUPSIZE = Runtime.getRuntime().availableProcessors();

    // Set of all channel instances created.
    private Set<Channel> bridgeChannelSet;

    // Worker group for handling I/O events. No separate acceptor group required since expected number of connections is
    // low.
    private EventLoopGroup workerGroup;

    protected AbstractBridgeProvider()
    {
        this(DEFAULT_WORKER_GROUPSIZE);
    }

    /**
     * Constructor.
     */
    protected AbstractBridgeProvider(int workerGroupSize)
    {
        this.bridgeChannelSet = new HashSet<Channel>();
        this.workerGroup = generateEventLoopGroup(DEFAULT_WORKER_GROUPSIZE);

    }

    /**
     * Creates and binds a service end-point with the provided service USN stack. This method presumes that service has
     * provided extended {@link USNPipelineInitializer} with the service specific stack and protocol handlers.
     * 
     * @param servicePort
     *            - a valid service port to bind the service on or 0 to retrieve a wild-card port.
     * @param pipelineInitializer
     *            - an instance of {@link USNPipelineInitializer} to initialize USN service stack to incoming
     *            connections.
     * @param listenerSet
     *            - a {@link Set}<{@link IChannelListener}> to notify with service life-cycle results.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    protected void provideServiceBridge(short servicePort, final USNPipelineInitializer pipelineInitializer,
        final Set<IChannelListener> listenerSet) throws BridgeException
    {
        LOG.enterMethod(ARG_SERVICE_PORT, servicePort, ARG_PIPELINE_INITIALIZER, pipelineInitializer);

        try
        {
            ArgsChecker.errorOnNull(pipelineInitializer, ARG_PIPELINE_INITIALIZER);
            ArgsChecker.errorOnNull(this.workerGroup, ARG_WORKER_GROUP);
            USNBridgeUtil.validateNetworkPort(servicePort);

            ServerBootstrap serverBootstrap = createBaseServiceUSNStack(
                new InetSocketAddress(Inet4Address.getLocalHost(), servicePort), this.workerGroup, pipelineInitializer);

            // Add server listeners.
            serverBootstrap.attr(LISTENER_ATR_KEY, new HashSet<IChannelListener>(listenerSet));

            /**
             * TODO: what do we do here?????
             */
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childOption(ChannelOption.SO_LINGER, DEFAULT_SOCKET_LINGER_SEC);

            // Bind and add listener.
            serverBootstrap.bind().addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if (future.isSuccess())
                    {
                        for (IChannelListener listener : listenerSet)
                        {
                            listener.notifyChannelUp();
                        }
                        bridgeChannelSet.add(future.channel());
                    }
                    else
                    {
                        for (IChannelListener listener : listenerSet)
                        {
                            listener.notifyChannelError();
                        }
                        LOG.error(ERROR_SOCKET_BIND, future.channel().remoteAddress());
                        throw new BridgeException(ERROR_SOCKET_BIND);
                    }
                }
            });
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new BridgeException(ERROR_ILLEGAL_ARGUMENT, ie);
        }
        catch (UnknownHostException uhe)
        {
            LOG.error(ERROR_UNKNOWN_HOST, uhe);
            throw new BridgeException(ERROR_UNKNOWN_HOST, uhe);
        }
        catch (Exception e)
        {
            LOG.error(ERROR_GENERAL_EXCEPTION, e);
            throw new BridgeException(ERROR_GENERAL_EXCEPTION, e);
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    /**
     * Construct a new server service bootstrap. Bootstrap pipeline initializer should have been extended with the
     * service specific pipeline and protocol.
     * 
     * @param address
     *            - a valid local-host {@link SocketAddress}.
     * @param workerGroup
     *            - an initialized {@link EventLoopGroup} for handling I/O events.
     * @param pipelineInitializer
     *            - an instance of {@link USNPipelineInitializer} to provide new network stack for incoming connections.
     *            Service should have extended initializer by providing service specific pipeline items and protocol.
     * 
     * @return - a ready to bind {@link ServerBootstrap}.
     */
    private ServerBootstrap createBaseServiceUSNStack(SocketAddress address, EventLoopGroup workerGroup,
        final USNPipelineInitializer pipelineInitializer)
    {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(generateServerChannel());
        serverBootstrap.group(workerGroup);
        serverBootstrap.localAddress(address);
        serverBootstrap.childHandler(pipelineInitializer);
        return serverBootstrap;
    }

    // private Bootstrap createBaseClientUSNStack(SocketAddress address, EventLoopGroup workerGroup)
    // {
    // Bootstrap bootstrap = new Bootstrap();
    // bootstrap.channel(generateServerChannel());
    // bootstrap.group(workerGroup);
    // bootstrap.remoteAddress(address);
    // return bootstrap;
    // }

    /**
     * Shut down USN bridge layer. Notify channel listeners and close channels.
     * 
     * @throws BridgeException
     *             - throws {@link BridgeException} on error.
     */
    protected void doShutdown() throws BridgeException
    {
        LOG.enterMethod();
        for (Channel channel : this.bridgeChannelSet)
        {
            try
            {
                Set<IChannelListener> listenerSet = channel.attr(LISTENER_ATR_KEY).get();
                if (listenerSet != null)
                {
                    for (IChannelListener listener : listenerSet)
                    {
                        listener.notifyChannelDown();
                    }
                }
                channel.close().syncUninterruptibly();
            }
            catch (NullPointerException npe)
            {
                LOG.error(ERROR_NPE, npe);
                throw new BridgeException(ERROR_NPE, npe);
            }
            catch (ChannelException ce)
            {
                LOG.error(ERROR_CHANNEL_EXCEPTION, ce);
                throw new BridgeException(ERROR_CHANNEL_EXCEPTION, ce);
            }
        }
        this.bridgeChannelSet.clear();

        LOG.enterMethod();
    }

    /**
     * Helper method for returning {@link EventLoopGroup} implementation. Attempt to use more efficient epoll on Linux
     * systems.
     * 
     * @param numThreads
     *            - amount of threads to allocate for returned loop group.
     * @return implementation of {@link EventLoopGroup}.
     */
    private EventLoopGroup generateEventLoopGroup(int numThreads)
    {
        return PlatformDependent.isWindows() ? new NioEventLoopGroup(numThreads) : new EpollEventLoopGroup(numThreads);
    }

    /**
     * Helper method for returning subclass of {@link ServerChannel} class. Attempt to use more efficient epoll on Linux
     * systems.
     * 
     * @return subclass of {@link ServerChannel} . Attempts to use epoll socket channel for higher efficiency.
     */
    private Class<? extends ServerChannel> generateServerChannel()
    {
        return PlatformDependent.isWindows() ? NioServerSocketChannel.class : EpollServerSocketChannel.class;
    }
}
