/**
 * @file IIdentifiable.java
 * @brief Interface defining capability for object to be uniquely identifiable on transport layer.
 */

package platform.bridge.api.proxy.transport;

import java.util.UUID;

/**
 * Interface defining capability for object to be uniquely identifiable on transport layer.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface ITransportIdentifiable
{
    /**
     * Set unique transport id to an object.
     * 
     * @param id
     *            - a {@link UUID} unique transport id.
     */
    void setTransportId(UUID id);

    /**
     * Retrieve unique transport id of an object.
     * 
     * @return - a {@link UUID} unique transport id.
     */
    UUID getTransportId();
}
