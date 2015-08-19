/**
 * @file AbstractPlatformClientProxy.java
 * @brief <description>
 */

package platform.bridge.proxy;

import game.usn.bridge.api.protocol.AbstractPacket;
import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import game.usn.bridge.pipeline.ChannelOptions;
import game.usn.bridge.proxy.AbstractDataProxy;
import io.netty.channel.ChannelHandler;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import platform.core.api.exception.BridgeException;
import platform.service.api.IServiceProxy;

public abstract class AbstractPlatformClientProxy extends AbstractDataProxy implements IServiceProxy
{
    private static final ExecutorService CLIENT_THREAD_POOL = Executors.newFixedThreadPool(2);

    private static final AtomicInteger REF_COUNT = new AtomicInteger(0);

    private CountDownLatch latch = new CountDownLatch(1);

    protected AbstractPlatformClientProxy()
    {
        super();
    }

    @Override
    public synchronized void initialize(InetAddress targetServiceAddress) throws BridgeException
    {
        if (!initialized.get())
        {
            super.initialize(targetServiceAddress.getHostAddress(), servicePort);
            REF_COUNT.addAndGet(1);
        }
    }

    @Override
    public synchronized void release() throws BridgeException
    {
        if (initialized.get())
        {
            super.release();
            if (REF_COUNT.decrementAndGet() <= 0)
            {
                CLIENT_THREAD_POOL.shutdownNow();
            }
        }
    }

    protected final Object send(AbstractPacket packet) throws BridgeException, InterruptedException
    {
        channel.writeAndFlush(packet).awaitUninterruptibly(2000);
        if (!latch.await(3, TimeUnit.SECONDS))
        {
            throw new BridgeException("");
        }
        else
        {
            return new Object();
        }
    }

    protected final void notify(AbstractPacket packet) throws BridgeException
    {

    }

    // Provided instance of protocol to use.
    private AbstractUSNProtocol consumerProtocol;

    // Proxy implementation specific in and out handler lists.
    private List<ChannelHandler> inHandlers;
    private List<ChannelHandler> outHandlers;

    // Provided channel options specific per proxy.
    private ChannelOptions channelOptions;

}
