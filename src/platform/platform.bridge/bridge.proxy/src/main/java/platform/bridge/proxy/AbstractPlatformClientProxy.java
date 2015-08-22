/**
 * @file AbstractPlatformClientProxy.java
 * @brief Abstract platform client proxy provides a basic proxy functionality for interacting with a remote service.
 */

package platform.bridge.proxy;

import game.usn.bridge.api.protocol.AbstractPacket;
import game.usn.bridge.api.proxy.IIdentifiable;
import game.usn.bridge.proxy.AbstractBridgeAdapter;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import platform.core.api.exception.BridgeException;

/**
 * Abstract platform client proxy provides a basic client proxy functionality for interacting with a remote service. It
 * provides a synchronous and asynchronous data transmission with remote service.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public abstract class AbstractPlatformClientProxy extends AbstractBridgeAdapter
{
    // Request future map. Maps unique synchronous message id to its request future.
    private static ConcurrentHashMap<UUID, RequestFuture> requestFutureMap = new ConcurrentHashMap<UUID, RequestFuture>();

    /**
     * Constructor.
     */
    protected AbstractPlatformClientProxy()
    {
        super();
    }

    /**
     * Synchronously send a packet to remote host. Invoking this method will block until a response has been received or
     * until a timeout occurs.
     * 
     * @param identifiablePacket
     *            - a source packet to send. Must extend {@link AbstractPacket} to be compatible with protocol and
     *            {@link IIdentifiable} to provide a unique message id.
     * @return - receive response. Response must be a complex {@link AbstractPacket} in accordance with the protocol.
     * @throws BridgeException
     *             - throws {@link BridgeException} on network write failure or on request timeout.
     */
    protected final <T extends AbstractPacket & IIdentifiable> AbstractPacket send(T identifiablePacket)
        throws BridgeException
    {
        UUID packetId = UUID.randomUUID();
        identifiablePacket.setId(packetId);

        RequestFuture requestFuture = new RequestFuture(packetId);
        requestFutureMap.put(packetId, requestFuture);

        try
        {
            super.sendPacket(identifiablePacket);
            return requestFuture.get(2, TimeUnit.SECONDS);
        }
        catch (BridgeException be)
        {
            requestFutureMap.remove(requestFuture);
            throw be;
        }
    }

    /**
     * Notify remote service with data. This call is purely asynchronous.
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
     * {@inheritDoc} Determine if packet has been received for a synchronous request. Notify the future if synchronous
     * request and forward to upstream packet handler if asynchronous.
     */
    @Override
    protected void receive(AbstractPacket abstractPacket)
    {
        UUID packetId = null;
        if (abstractPacket instanceof IIdentifiable)
        {
            packetId = ((IIdentifiable) abstractPacket).getId();
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
                future.response(abstractPacket);
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
