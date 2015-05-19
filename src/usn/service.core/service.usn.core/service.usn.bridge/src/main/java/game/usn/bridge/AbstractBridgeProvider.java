/**
 * @file AbstractBridgeProvider.java
 * @brief AbstractBridgeProvider attempts to provide base functionality for unified service network.
 */

package game.usn.bridge;

import game.core.util.ArgsChecker;
import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.listener.IServerListener;
import game.usn.bridge.pipeline.USNPipelineInitializer;
import game.usn.bridge.util.USNBridgeUtil;
import io.netty.bootstrap.Bootstrap;
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
    // Errors, args, messages.
    private static final String ARG_PIPELINE_INITIALIZED = "pipelineInitializer";
    private static final String ARG_WORKER_GROUP = "workerGroup";
    private static final String ERROR_SOCKET_BIND = "Error binding server socket.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_UNKNOWN_HOST = "Cannot retrieve hostname for socket bind.";
    private static final String ERROR_GENERAL_EXCEPTION = "General exception thrown.";

    // Default socket linger duration in seconds.
    private static final int DEFAULT_SOCKET_LINGER_SEC = 3;

    // Default server group size.
    private static final int DEFAULT_SERVER_GROUPSIZE = 1;

    // Default worker group size.
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
        this.workerGroup = generateEventLoopGroup(DEFAULT_SERVER_GROUPSIZE);

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
     * @param serverListenerSet
     *            - a {@link Set}<{@link IServerListener}> to notify with service bind result.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    protected void provideServiceBridge(short servicePort, final USNPipelineInitializer pipelineInitializer,
        final Set<IServerListener> serverListenerSet)
        throws BridgeException
    {
        try
        {
            ArgsChecker.errorOnNull(pipelineInitializer, ARG_PIPELINE_INITIALIZED);
            ArgsChecker.errorOnNull(this.workerGroup, ARG_WORKER_GROUP);
            USNBridgeUtil.validateNetworkPort(servicePort);

            ServerBootstrap serverBootstrap = createBaseServiceUSNStack(
                new InetSocketAddress(Inet4Address.getLocalHost(), servicePort), this.workerGroup, pipelineInitializer);
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childOption(ChannelOption.SO_LINGER, DEFAULT_SOCKET_LINGER_SEC);
            serverBootstrap.bind().addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if (future.isSuccess())
                    {
                        for (IServerListener listener : serverListenerSet)
                        {
                            listener.notifyServerUp();
                        }
                        bridgeChannelSet.add(future.channel());
                    }
                    else
                    {
                        throw new BridgeException(ERROR_SOCKET_BIND);
                    }
                }
            });
        }
        catch (IllegalArgumentException ie)
        {
            throw new BridgeException(ERROR_ILLEGAL_ARGUMENT, ie);
        }
        catch (UnknownHostException uhe)
        {
            throw new BridgeException(ERROR_UNKNOWN_HOST, uhe);
        }
        catch (Exception e)
        {
            throw new BridgeException(ERROR_GENERAL_EXCEPTION, e);
        }
    }

    /**
     * Create a new server bootstrap for provided service USN stack.
     * 
     * @param address
     *            - a valid local {@link SocketAddress}.
     * @param workerGroup
     *            - an initialized {@link EventLoopGroup} for handling I/O events.
     * @param pipelineInitializer
     *            - an instance of {@link USNPipelineInitializer} to provide a minimum of base USN stack to new
     *            connections. By default, USNPipelineProvider will provide only base USN specific network stack which
     *            cannot be modified. Service proxy should extend base stack with service specific protocol and message
     *            handlers.
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

    private Bootstrap createBaseClientUSNStack(SocketAddress address, EventLoopGroup workerGroup)
    {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(generateServerChannel());
        bootstrap.group(workerGroup);
        bootstrap.remoteAddress(address);
        return bootstrap;
    }

    protected void doShutdown()
    {
        for (Channel channel : this.bridgeChannelSet)
        {
            try
            {
                channel.close().syncUninterruptibly();
            }
            catch (ChannelException ce)
            {

            }
        }
        this.bridgeChannelSet.clear();
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
