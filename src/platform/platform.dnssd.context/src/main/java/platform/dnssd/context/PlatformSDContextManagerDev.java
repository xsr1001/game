/**
 * @file PlatformSDContextManagerDev.java
 * @brief Platform SD context manager for development context.
 */

package platform.dnssd.context;

import platform.dnssd.api.context.IPlatformSDContextManager;

/**
 * Platform SD context manager for development context.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformSDContextManagerDev implements IPlatformSDContextManager
{
    @Override
    public String getPlatformId()
    {
        return "dev";
    }

    @Override
    public String getDomain()
    {
        return "local";
    }
}
