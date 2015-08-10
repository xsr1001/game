/**
 * @file PlatformSDContextManagerProd.java
 * @brief Platform SD context manager for production context.
 */

package platform.dnssd.context;

import platform.dnssd.api.context.IPlatformSDContextManager;

/**
 * Platform SD context manager for production context.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformSDContextManagerProd implements IPlatformSDContextManager
{
    @Override
    public String getPlatformId()
    {
        return "prod";
    }

    @Override
    public String getDomain()
    {
        return "local";
    }
}
