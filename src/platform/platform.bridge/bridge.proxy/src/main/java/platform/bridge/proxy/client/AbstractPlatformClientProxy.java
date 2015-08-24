/**
 * @file AbstractPlatformClientProxy.java
 * @brief Abstract platform client proxy provides a basic proxy functionality for interacting with a remote service.
 */

package platform.bridge.proxy.client;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import platform.bridge.api.protocol.AbstractPacket;
import platform.bridge.api.proxy.ITransportIdentifiable;
import platform.core.api.exception.BridgeException;

/**
 * Abstract platform client proxy provides a basic client proxy functionality for interacting with a remote service. It
 * provides a synchronous and asynchronous data transmission with remote service.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractPlatformClientProxy extends AbstractClientProxy
{
    // Default wait time for a synchronous response.
    private static final int DEFAULT_RESPONSE_WAIT_TIME_SEC = 2;

    // Request future map, mapping unique packet id to its request future.
    private ConcurrentHashMap<UUID, RequestFuture> requestFutureMap;

    /**
     * Constructor.
     */
    protected AbstractPlatformClientProxy()
    {
        super();
        requestFutureMap = new ConcurrentHashMap<UUID, RequestFuture>();
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
            super.sendPacket(packet);
            return requestFuture.get(DEFAULT_RESPONSE_WAIT_TIME_SEC, TimeUnit.SECONDS);
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
        super.sendPacket(packet);
    }

    /**
     * {@inheritDoc} Determine if packet received originated from a synchronous request. If yes, notify the future with
     * result otherwise notify proxy with data.
     */
    @Override
    protected final void receive(AbstractPacket abstractPacket)
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
