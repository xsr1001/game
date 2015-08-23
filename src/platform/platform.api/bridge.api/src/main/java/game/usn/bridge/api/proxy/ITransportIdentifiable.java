/**
 * @file IIdentifiable.java
 * @brief <description>
 */

package game.usn.bridge.api.proxy;

import java.util.UUID;

public interface ITransportIdentifiable
{
    void setTransportId(UUID id);

    UUID getTransportId();
}
