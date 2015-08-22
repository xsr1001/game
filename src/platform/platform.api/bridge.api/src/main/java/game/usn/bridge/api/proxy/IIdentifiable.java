/**
 * @file IIdentifiable.java
 * @brief <description>
 */

package game.usn.bridge.api.proxy;

import java.util.UUID;

public interface IIdentifiable
{
    void setId(UUID id);

    UUID getId();
}
