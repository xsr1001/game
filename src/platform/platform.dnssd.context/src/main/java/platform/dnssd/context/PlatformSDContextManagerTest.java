/**
 * @file PlatformSDContextManagerTest.java
 * @brief Platform SD context manager for test context.
 */

package platform.dnssd.context;

import platform.dnssd.api.context.IPlatformSDContextManager;

/**
 * Platform SD context manager for test context.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformSDContextManagerTest implements IPlatformSDContextManager
{
    @Override
    public String getPlatformId()
    {
        return "test";
    }

    @Override
    public String getDomain()
    {
        return "local";
    }
}
