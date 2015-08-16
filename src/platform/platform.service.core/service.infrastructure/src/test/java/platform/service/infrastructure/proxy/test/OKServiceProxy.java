/**
 * @file OKServiceProxy.java
 * @brief Test class for service proxy with OK initialization.
 */

package platform.service.infrastructure.proxy.test;

import java.net.InetAddress;

import platform.core.api.exception.BridgeException;
import platform.service.api.IServiceProxy;

/**
 * Test class for service proxy with OK initialization.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class OKServiceProxy implements IServiceProxy
{
    public OKServiceProxy()
    {}

    @Override
    public void initialize(InetAddress serviceAddress) throws BridgeException
    {}

    @Override
    public void release() throws BridgeException
    {

    }
}
