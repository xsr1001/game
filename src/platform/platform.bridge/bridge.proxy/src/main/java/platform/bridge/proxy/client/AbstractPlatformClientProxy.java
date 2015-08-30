/**
 * @file AbstractPlatformClientProxy.java
 * @brief Abstract platform client proxy provides a basic platform client proxy functionality for interacting with a remote service.
 */

package platform.bridge.proxy.client;

import game.core.util.ArgsChecker;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.proxy.IClientProxyBase;
import platform.bridge.api.proxy.IResponseListener;
import platform.bridge.api.proxy.transport.ITransportIdentifiable;
import platform.core.api.exception.BridgeException;

/**
 * Abstract platform client proxy provides a basic platform client proxy functionality for interacting with a remote
 * service. It provides a synchronous and asynchronous data transmission with remote service.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractPlatformClientProxy implements IResponseListener
{
    // Args.
    private static final String ARG_CLIENT_PROXY_BASE = "clientProxyBase";
    private static final String ARG_SERVICE_IP = "serviceIPv4";
    private static final String ARG_SERVICE_PORT = "servicePort";

    // Default wait time for a synchronous response in seconds.
    private static final int DEFAULT_RESPONSE_WAIT_TIME_SEC = 2;

    // Request future map, mapping unique packet id to its request future.
    private ConcurrentHashMap<UUID, RequestFuture> requestFutureMap;

    // Network base specific client proxy base implementation.
    private IClientProxyBase clientProxyBase;

    // Amount of seconds to block waiting for respons to synchronous request.
    private int responseWaitTimeSec;

    /**
     * Constructor.
     * 
     * @param clientProxyBase
     *            - a {@link IClientProxyBase} client proxy base implementation.
     */
    protected AbstractPlatformClientProxy(IClientProxyBase clientProxyBase)
    {
        this(clientProxyBase, DEFAULT_RESPONSE_WAIT_TIME_SEC);
    }

    /**
     * 
     * @param clientProxyBase
     *            - a {@link IClientProxyBase} client proxy base implementation.
     * @param responseWaitTimeSec
     *            - a time amount to wait for responses to synchronous requests.
     */
    protected AbstractPlatformClientProxy(IClientProxyBase clientProxyBase, int responseWaitTimeSec)
    {
        ArgsChecker.errorOnNull(clientProxyBase, ARG_CLIENT_PROXY_BASE);

        this.clientProxyBase = clientProxyBase;
        this.responseWaitTimeSec = responseWaitTimeSec;

        requestFutureMap = new ConcurrentHashMap<UUID, RequestFuture>();
    }

    /**
     * Initialized platform client proxy.
     * 
     * @param serviceIPv4Address
     *            - a {@link String} service IPv4 address to connect with.
     * @param servicePort
     *            - a {@link Integer} service port to connect on.
     * @throws BridgeException
     *             - throws {@link BridgeException} on platform client proxy initialization failure.
     */
    public void initialize(String serviceIPv4Address, Integer servicePort) throws BridgeException
    {
        ArgsChecker.errorOnNull(serviceIPv4Address, ARG_SERVICE_IP);
        ArgsChecker.errorOnNull(servicePort, ARG_SERVICE_PORT);

        clientProxyBase.initialize(serviceIPv4Address, servicePort, this);
    }

    /**
     * Release platform client proxy and cleanup.
     * 
     * @throws BridgeException
     *             - throws {@link BridgeException} on release failure.
     */
    public void release() throws BridgeException
    {
        clientProxyBase.release();
        for (RequestFuture requestFuture : requestFutureMap.values())
        {
            requestFuture.cancel();
        }
        requestFutureMap.clear();
    }

    /**
     * Synchronously send data to remote service. Invoking this method will block until a response has been received or
     * a timeout occurs.
     * 
     * @param packet
     *            - a source packet to send. Must extend {@link AbstractPacket} to be compatible with protocol and
     *            implement {@link ITransportIdentifiable} to provide a unique transport packet id.
     * @return - a {@link AbstractPacket} received response.
     * @throws BridgeException
     *             - throws {@link BridgeException} on network send failure or on response wait timeout.
     */
    protected final <T extends AbstractPacket & ITransportIdentifiable> AbstractPacket send(T packet)
        throws BridgeException
    {
        UUID packetTransportId = UUID.randomUUID();
        packet.setTransportId(packetTransportId);

        RequestFuture requestFuture = new RequestFuture();
        requestFutureMap.put(packetTransportId, requestFuture);

        try
        {
            clientProxyBase.sendPacket(packet);
            return requestFuture.get(responseWaitTimeSec, TimeUnit.SECONDS);
        }
        catch (BridgeException be)
        {
            throw be;
        }
        finally
        {
            requestFutureMap.remove(requestFuture);
        }
    }

    /**
     * Asynchronously send data to remote service. This call is purely asynchronous and will not block.
     * 
     * @param packet
     *            - a source {@link AbstractPacket} to notify remote service with.
     * @throws BridgeException
     *             - throws {@link BridgeException} on network write failure.
     */
    protected final void notify(AbstractPacket packet) throws BridgeException
    {
        clientProxyBase.sendPacket(packet);
    }

    /**
     * {@inheritDoc} Determine if packet received originated from a synchronous request. If yes, notify the future with
     * result otherwise notify proxy with data.
     */
    @Override
    public final void receive(AbstractPacket abstractPacket)
    {
        UUID packetId = null;
        if (abstractPacket instanceof ITransportIdentifiable)
        {
            packetId = ((ITransportIdentifiable) abstractPacket).getTransportId();
        }

        if (packetId == null)
        {
            receivePacket(abstractPacket);
            return;
        }
        else
        {
            RequestFuture future = requestFutureMap.get(packetId);
            if (future != null)
            {
                future.result(abstractPacket);
            }
        }
    }

    /**
     * Receive an asynchronous response from the remote service.
     * 
     * @param abstractPacket
     *            - a {@link AbstractPacket} response.
     */
    protected abstract void receivePacket(AbstractPacket abstractPacket);
}
