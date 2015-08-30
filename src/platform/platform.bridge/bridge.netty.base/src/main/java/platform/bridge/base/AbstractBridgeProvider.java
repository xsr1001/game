/**
 * @file AbstractBridgeProvider.java
 * @brief AbstractBridgeProvider attempts to provide base functionality for network base operations
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
import io.netty.channel.ChannelFutureListener;
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

import platform.bridge.api.listener.IChannelObserver;
import platform.bridge.api.proxy.ChannelOptions;
import platform.bridge.base.pipeline.PlatformPipelineInitializer;
import platform.bridge.base.proxy.AbstractBridgeAdapter;
import platform.core.api.exception.BridgeException;

/**
 * Abstract bridge provider provides base functionality for network base operations for various consumers. It provides
 * functionality for initializing consumer specific protocol stacks.
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
    private static final String ARG_CHANNEL_OPTIONS = "channelOptions";
    private static final String ARG_REMOTE_ADDRESS = "remoteAddress";
    private static final String ERROR_SOCKET_BIND = "Error binding server socket.";
    private static final String ERROR_CONNECT = "Error connecting with remote host: [%s].";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_UNKNOWN_HOST = "Cannot retrieve hostname for socket bind.";
    private static final String ERROR_GENERAL_EXCEPTION = "General exception thrown.";
    private static final String ERROR_NPE = "Null pointer exception thrown.";
    private static final String ERROR_CHANNEL_EXCEPTION = "Channel exception thrown.";
    private static final String ERROR_OBSERVER_NULL_PARAM = "Cannot notify observers due to null parameter.";
    protected static final String WARN_OBSERVER_EXCEPTION = "Exception thrown while notifying channel listener. Guarding bridge...";

    // Default worker group size.
    private static final int DEFAULT_WORKER_GROUPSIZE = Runtime.getRuntime().availableProcessors();

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
     * Creates and binds a service end-point with the provided service USN stack. This method presumes that service has
     * provided extended {@link PlatformPipelineInitializer} with the service specific stack and protocol handlers.
     * 
     * @param servicePort
     *            - a valid service port to bind the service on or 0 to retrieve a wild-card port.
     * @param pipelineInitializer
     *            - an instance of {@link PlatformPipelineInitializer} to initialize platform service stack to incoming
     *            connections.
     * @param observerSet
     *            - a {@link Set}<{@link IChannelObserver}> to notify with server socket channel life-cycle events.
     * @param channelOptions
     *            - a {@link ChannelOptions} options for child channels containing consumer specific options.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    protected void provideServiceBridge(int servicePort, final PlatformPipelineInitializer pipelineInitializer,
        final Set<IChannelObserver> observerSet, final ChannelOptions channelOptions) throws BridgeException
    {
        LOG.enterMethod(ARG_SERVICE_PORT, servicePort, ARG_PIPELINE_INITIALIZER, pipelineInitializer,
            ARG_CHANNEL_OPTIONS, channelOptions);

        try
        {
            ArgsChecker.errorOnNull(pipelineInitializer, ARG_PIPELINE_INITIALIZER);
            ArgsChecker.errorOnNull(channelOptions, ARG_CHANNEL_OPTIONS);
            ArgsChecker.errorOnNull(this.workerGroup, ARG_WORKER_GROUP);
            NetworkUtils.validateNetworkPort(servicePort);

            ServerBootstrap serverBootstrap = createBaseServiceUSNStack(
                new InetSocketAddress(Inet4Address.getLocalHost(), servicePort), this.workerGroup, pipelineInitializer);

            // Add server channel listeners.
            serverBootstrap.attr(PlatformPipelineInitializer.CHANNEL_OBSERVER_ATR_KEY, new HashSet<IChannelObserver>(
                observerSet));

            // Add channel options.
            serverBootstrap.childAttr(PlatformPipelineInitializer.CHANNEL_OPTIONS_ATR_KEY, channelOptions);


            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childOption(ChannelOption.SO_LINGER, 5);

            // Bind and add listener.
            serverBootstrap.bind().addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if (future.isSuccess())
                    {
                        notifyObservers(observerSet, pipelineInitializer.getConsumerProxy().getName(),
                            (InetSocketAddress) future.channel().localAddress(), true);

                        bridgeChannelSet.add(future.channel());
                    }
                    else
                    {
                        LOG.error(ERROR_SOCKET_BIND);
                        notifyObservers(observerSet, pipelineInitializer.getConsumerProxy().getName(), null, false);
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
     * Creates an outgoing channel and connects with the remote host. This method presumes that client has provided
     * extended {@link PlatformPipelineInitializer} with the client specific stack and protocol handlers.
     * 
     * @param remoteAddress
     *            - a valid remote-host {@link SocketAddress}.
     * @param pipelineInitializer
     *            - an instance of {@link PlatformPipelineInitializer} to initialize platform client stack for outgoing
     *            connection.
     * @param observerSet
     *            - a {@link Set}<{@link IChannelObserver}> to notify with client socket channel life-cycle events.
     * @param channelOptions
     *            - a {@link ChannelOptions} options for child channels containing consumer specific options.
     * @throws BridgeException
     *             - throw {@link BridgeException} on error.
     */
    protected void provideClientBridge(final SocketAddress remoteAddress,
        final PlatformPipelineInitializer pipelineInitializer, final Set<IChannelObserver> observerSet,
        final ChannelOptions channelOptions) throws BridgeException
    {
        LOG.enterMethod(ARG_PIPELINE_INITIALIZER, pipelineInitializer, ARG_CHANNEL_OPTIONS, channelOptions);

        try
        {
            ArgsChecker.errorOnNull(pipelineInitializer, ARG_PIPELINE_INITIALIZER);
            ArgsChecker.errorOnNull(channelOptions, ARG_CHANNEL_OPTIONS);
            ArgsChecker.errorOnNull(remoteAddress, ARG_REMOTE_ADDRESS);
            ArgsChecker.errorOnNull(this.workerGroup, ARG_WORKER_GROUP);

            Bootstrap clientBootstrap = createBaseClientUSNStack(remoteAddress, workerGroup, pipelineInitializer);

            // Add channel listeners.
            clientBootstrap.attr(PlatformPipelineInitializer.CHANNEL_OBSERVER_ATR_KEY, new HashSet<IChannelObserver>(
                observerSet));

            // Add channel options.
            clientBootstrap.attr(PlatformPipelineInitializer.CHANNEL_OPTIONS_ATR_KEY, channelOptions);

            clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
            clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            clientBootstrap.option(ChannelOption.SO_LINGER, 5);

            // Connect and add listener.
            clientBootstrap.connect().addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if (future.isSuccess())
                    {
                        notifyObservers(observerSet,
                            future.channel().pipeline().get(AbstractBridgeAdapter.class).getName(),
                            (InetSocketAddress) future.channel().remoteAddress(), true);

                        bridgeChannelSet.add(future.channel());
                    }
                    else
                    {
                        notifyObservers(observerSet,
                            future.channel().pipeline().get(AbstractBridgeAdapter.class).getName(), null, false);
                        LOG.error(String.format(ERROR_CONNECT, future.channel().remoteAddress()));
                        return;
                    }
                }
            });
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new BridgeException(ERROR_ILLEGAL_ARGUMENT, ie);
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
     *            - an instance of {@link PlatformPipelineInitializer} to provide new network stack for incoming connections.
     *            Service should have extended initializer by providing service specific pipeline items and protocol.
     * 
     * @return - a ready to bind {@link ServerBootstrap}.
     */
    private ServerBootstrap createBaseServiceUSNStack(SocketAddress address, EventLoopGroup workerGroup,
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
     *            - an instance of {@link PlatformPipelineInitializer} to provide new network stack for remote connections.
     *            Client should have extended initializer by providing client specific pipeline items and protocol.
     * 
     * @return - a ready to connect {@link Bootstrap}.
     */
    private Bootstrap createBaseClientUSNStack(SocketAddress address, EventLoopGroup workerGroup,
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
                Set<IChannelObserver> observerSet = channel.attr(PlatformPipelineInitializer.CHANNEL_OBSERVER_ATR_KEY).get();
                if (observerSet != null)
                {
                    for (IChannelObserver observer : observerSet)
                    {
                        // Guard bridge from observer exceptions.
                        try
                        {
                            observer.notifyChannelDown(channel.pipeline().get(AbstractBridgeAdapter.class).getName());
                        }
                        catch (NullPointerException npe)
                        {
                            LOG.error(ERROR_OBSERVER_NULL_PARAM, npe);
                        }
                        catch (Exception e)
                        {
                            LOG.warn(WARN_OBSERVER_EXCEPTION, e);
                        }
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
     * Notify channel observers with the bridge operation result.
     * 
     * @param observerSet
     *            - a {@link Set} of{@link IChannelObserver} objects representing observer to notify.
     * @param proxyName
     *            - a {@link String} unique proxy name.
     * @param address
     *            - a {@link InetSocketAddress} on which bridge operation result received.
     * @param success
     *            - determines success or failure of bridge operation.
     */
    protected void notifyObservers(Set<IChannelObserver> observerSet, String proxyName, InetSocketAddress address,
        boolean success)
    {
        if (observerSet != null)
        {
            for (IChannelObserver observer : observerSet)
            {
                // Guard bridge from observer exceptions.
                try
                {
                    if (success)
                    {
                        observer.notifyChannelUp(proxyName, address);
                    }
                    else
                    {
                        observer.notifyChannelDown(proxyName);
                    }
                }
                catch (NullPointerException npe)
                {
                    LOG.error(ERROR_OBSERVER_NULL_PARAM, npe);
                }
                catch (Exception e)
                {
                    LOG.warn(WARN_OBSERVER_EXCEPTION, e);
                }
            }
        }
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
