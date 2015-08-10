/**
 * @file IPlatformContextManager.java
 * @brief SD context manager capabilities interface.
 */

package platform.dnssd.api.context;

/**
 * SD context manager capabilities interface. Concrete implementations of this interface may provide specific context .
 * This provides a logical environment to perform service discovery in and prevents cross platform service discovery.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public interface IPlatformSDContextManager
{
    /**
     * Retrieve a platform identifier. Platform instance is a logical encapsulation of service discovery context.
     * Platform identifier will be used as a concrete sub-domain for registering new services.
     * 
     * @return - a {@link String} unique platform identifier.
     */
    String getPlatformId();

    /**
     * A temporary method to retrieve DNS domain to advertise services on. This functionality should be removed once an
     * infrastructure to provide local network DNS capabilities.
     * 
     * @return
     */
    String getDomain();
}
