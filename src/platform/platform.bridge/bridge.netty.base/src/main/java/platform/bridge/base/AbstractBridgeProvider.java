/**
 * @file AbstractBridgeProvider.java
 * @brief AbstractBridgeProvider attempts to provide base functionality for network base operations via network network base.
 */

package platform.bridge.base;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;
import game.core.util.NetworkUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.PlatformDependent;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import platform.bridge.api.proxy.BridgeOptions;
import platform.bridge.base.pipeline.PlatformPipelineInitializer;
import platform.core.api.exception.BridgeException;

/**
 * Abstract bridge provider provides base functionality for network base operations for various consumers via network
 * network base. It provides functionality for initializing consumer specific protocol stacks.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractBridgeProvider
{
    // Logger.
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractBridgeProvider.class);

    // Errors, args, messages.
    private static final String ARG_WORKER_GROUP = "workerGroup";
    private static final String ARG_PIPELINE_INITIALIZER = "pipelineInitializer";
    private static final String ARG_SERVICE_PORT = "servicePort";
    private static final String ARG_BRIDGE_OPTIONS = "bridgeOptions";
    private static final String ARG_REMOTE_ADDRESS = "remoteAddress";
    private static final String ERROR_SOCKET_BIND = "Error binding server socket on [%s].";
    private static final String ERROR_CONNECT = "Error connecting with remote host: [%s].";
    private static final String ERROR_UNKNOWN_HOST = "Cannot retrieve hostname for socket bind.";
    private static final String ERROR_INTERRUPTED_EXCEPTION = "Interrupted exception thrown while blocking.";
    private static final String ERROR_GENERAL_EXCEPTION = "General exception thrown.";
    private static final String ERROR_CHANNEL_EXCEPTION = "Channel exception thrown.";
    protected static final String WARN_OBSERVER_EXCEPTION = "Exception thrown while notifying channel listener. Guarding bridge...";
    private static final String MSG_SERVICE_BOUND = "Successfully bound a service on address: [%s].";
    private static final String MSG_CLIENT_CONNECTED = "Successfully connected with a remote service: [%s].";

    // Default wait time for channel bind and connect operations in seconds.
    private static final Integer DEFAULT_CHANNEL_WAIT_TIME_SEC = 5;

    // Default worker group size.
    private static final Integer DEFAULT_WORKER_GROUPSIZE = Runtime.getRuntime().availableProcessors();

    // Set of all channel instances created.
    private Set<Channel> bridgeChannelSet;

    // Worker group for handling I/O events. No separate acceptor group required since expected number of connections is
    // low.
    private EventLoopGroup workerGroup;

    /**
     * Ctor.
     */
    protected AbstractBridgeProvider()
    {
        this(DEFAULT_WORKER_GROUPSIZE);
    }

    /**
     * Ctor.
     * 
     * @param workerGroupSize
     *            - number of threads in event I/O processing group.
     */
    protected AbstractBridgeProvider(int workerGroupSize)
    {
        bridgeChannelSet = new HashSet<Channel>();
        workerGroup = generateEventLoopGroup(workerGroupSize);
    }

    /**
     * Creates and binds a service end-point with the provided service platform stack. This method presumes that service
     * has provided extended {@link PlatformPipelineInitializer} with the service specific stack and protocol handlers.
     * 
     * @param servicePort
     *            - a valid service port to bind the service on or 0 to retrieve a wild-card port.
     * @param pipelineInitializer
     *            - an instance of {@link PlatformPipelineInitializer} to initialize platform service stack to incoming
     *            connections.
     * @param bridgeOptions
     *            - a {@link BridgeOptions} options containing consumer specific options.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    protected int provideServiceBridge(int servicePort, final PlatformPipelineInitializer pipelineInitializer,
        final BridgeOptions bridgeOptions) throws BridgeException
    {
        LOG.enterMethod(ARG_SERVICE_PORT, servicePort, ARG_PIPELINE_INITIALIZER, pipelineInitializer,
            ARG_BRIDGE_OPTIONS, bridgeOptions);

        ArgsChecker.errorOnNull(pipelineInitializer, ARG_PIPELINE_INITIALIZER);
        ArgsChecker.errorOnNull(bridgeOptions, ARG_BRIDGE_OPTIONS);
        ArgsChecker.errorOnNull(this.workerGroup, ARG_WORKER_GROUP);
        NetworkUtils.validateNetworkPort(servicePort);

        try
        {
            InetSocketAddress serviceAddress = new InetSocketAddress(Inet4Address.getLocalHost(), servicePort);
            ServerBootstrap serverBootstrap = createBaseServicePlatformStack(serviceAddress, this.workerGroup,
                pipelineInitializer);

            // Add channel options.
            serverBootstrap.childAttr(PlatformPipelineInitializer.BRIDGE_OPTIONS_ATR_KEY, bridgeOptions);

            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childOption(ChannelOption.SO_LINGER, 5);

            // Bind and add listener.
            ChannelFuture serverBindFuture = serverBootstrap.bind();
            if (serverBindFuture.await(DEFAULT_CHANNEL_WAIT_TIME_SEC, TimeUnit.SECONDS))
            {
                LOG.info(String.format(MSG_SERVICE_BOUND, serverBindFuture.channel().localAddress()));
                bridgeChannelSet.add(serverBindFuture.channel());
            }
            else
            {
                LOG.error(String.format(ERROR_SOCKET_BIND, serviceAddress));
                throw new BridgeException(String.format(ERROR_SOCKET_BIND, serviceAddress));
            }

            return ((InetSocketAddress) serverBindFuture.channel().localAddress()).getPort();
        }
        catch (InterruptedException iee)
        {
            LOG.error(ERROR_INTERRUPTED_EXCEPTION, iee);
            throw new BridgeException(ERROR_INTERRUPTED_EXCEPTION, iee);
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
     * Creates an outgoing channel and connects with the remote host. This method presumes that client has provided
     * extended {@link PlatformPipelineInitializer} with the client specific stack and protocol handlers.
     * 
     * @param remoteAddressa
     *            - a valid remote host {@link SocketAddress} address.
     * @param pipelineInitializer
     *            - an instance of {@link PlatformPipelineInitializer} to initialize platform client stack for outgoing
     *            connection.
     * @param bridgeOptions
     *            - a {@link BridgeOptions} options containing consumer specific options.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    protected int provideClientBridge(final SocketAddress remoteAddress,
        final PlatformPipelineInitializer pipelineInitializer, final BridgeOptions bridgeOptions)
        throws BridgeException
    {
        LOG.enterMethod(ARG_PIPELINE_INITIALIZER, pipelineInitializer, ARG_BRIDGE_OPTIONS, bridgeOptions);

        ArgsChecker.errorOnNull(pipelineInitializer, ARG_PIPELINE_INITIALIZER);
        ArgsChecker.errorOnNull(bridgeOptions, ARG_BRIDGE_OPTIONS);
        ArgsChecker.errorOnNull(remoteAddress, ARG_REMOTE_ADDRESS);
        ArgsChecker.errorOnNull(this.workerGroup, ARG_WORKER_GROUP);

        try
        {
            Bootstrap clientBootstrap = createBaseClientPlatformStack(remoteAddress, workerGroup, pipelineInitializer);

            // Add bridge options.
            clientBootstrap.attr(PlatformPipelineInitializer.BRIDGE_OPTIONS_ATR_KEY, bridgeOptions);

            // Add client options.
            clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
            clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            clientBootstrap.option(ChannelOption.SO_LINGER, 5);

            // Connect and add listener.
            ChannelFuture clientConnectFuture = clientBootstrap.connect();
            if (clientConnectFuture.await(DEFAULT_CHANNEL_WAIT_TIME_SEC, TimeUnit.SECONDS))
            {
                LOG.info(String.format(MSG_CLIENT_CONNECTED, clientConnectFuture.channel().remoteAddress().toString()));
                bridgeChannelSet.add(clientConnectFuture.channel());
            }
            else
            {
                LOG.error(String.format(ERROR_CONNECT, remoteAddress));
                throw new BridgeException(String.format(ERROR_CONNECT, remoteAddress));
            }

            return ((InetSocketAddress) clientConnectFuture.channel().localAddress()).getPort();
        }
        catch (InterruptedException iee)
        {
            LOG.error(ERROR_INTERRUPTED_EXCEPTION, iee);
            throw new BridgeException(ERROR_INTERRUPTED_EXCEPTION, iee);
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
     *            - an instance of {@link PlatformPipelineInitializer} to provide new network stack for incoming
     *            connections. Service should have extended initializer by providing service specific pipeline items and
     *            protocol.
     * 
     * @return - a ready to bind {@link ServerBootstrap}.
     */
    private ServerBootstrap createBaseServicePlatformStack(SocketAddress address, EventLoopGroup workerGroup,
        final PlatformPipelineInitializer pipelineInitializer)
    {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(generateServerChannel());
        serverBootstrap.group(workerGroup);
        serverBootstrap.localAddress(address);
        serverBootstrap.childHandler(pipelineInitializer);
        return serverBootstrap;
    }

    /**
     * Construct a new client bootstrap. Bootstrap pipeline initializer should have been extended with the client
     * specific pipeline and protocol.
     * 
     * @param address
     *            - a valid remote-host {@link SocketAddress}.
     * @param workerGroup
     *            - an initialized {@link EventLoopGroup} for handling I/O events.
     * @param pipelineInitializer
     *            - an instance of {@link PlatformPipelineInitializer} to provide new network stack for remote
     *            connections. Client should have extended initializer by providing client specific pipeline items and
     *            protocol.
     * 
     * @return - a ready to connect {@link Bootstrap}.
     */
    private Bootstrap createBaseClientPlatformStack(SocketAddress address, EventLoopGroup workerGroup,
        final PlatformPipelineInitializer pipelineInitializer)
    {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(generateClientChannel());
        bootstrap.group(workerGroup);
        bootstrap.remoteAddress(address);
        bootstrap.handler(pipelineInitializer);
        return bootstrap;
    }

    /**
     * Shut down netty network base layer.
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

                channel.close().syncUninterruptibly();
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

    /**
     * Helper method for returning subclass of {@link SocketChannel} class. Attempt to use more efficient epoll on Linux
     * systems.
     * 
     * @return subclass of {@link SocketChannel} . Attempts to use epoll socket channel for higher efficiency.
     */
    private Class<? extends SocketChannel> generateClientChannel()
    {
        return PlatformDependent.isWindows() ? NioSocketChannel.class : EpollSocketChannel.class;
    }
}
