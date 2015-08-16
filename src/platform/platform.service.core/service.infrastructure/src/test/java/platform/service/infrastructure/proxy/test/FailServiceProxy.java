/**
 * @file FailServiceProxy.java
 * @brief  Test class for service proxy with failed initialization.
 */

package platform.service.infrastructure.proxy.test;

import java.net.InetAddress;

import platform.core.api.exception.BridgeException;
import platform.service.api.IServiceProxy;

/**
 * Test class for service proxy with failed initialization.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class FailServiceProxy implements IServiceProxy
{
    public FailServiceProxy()
    {}

    @Override
    public void initialize(InetAddress serviceAddress) throws BridgeException
    {
        throw new BridgeException("error");
    }

    @Override
    public void release() throws BridgeException
    {

    }
}
