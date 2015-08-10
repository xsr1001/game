/**
 * @file ISDEntity.java
 * @brief A service discovery entity interface provides type capabilities for entity to be advertised.
 */

package platform.dnssd.api.endpoint;

/**
 * A service discovery entity interface provides type capabilities for entity to be advertised.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface ISDEntity
{
    /**
     * Retrieve service type, associated with this SD entity.
     * 
     * @return - a {@link String} service type.
     */
    String getServiceType();
}
