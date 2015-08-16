/**
 * @file AbstractPlatformClientProxy.java
 * @brief <description>
 */

package platform.bridge.proxy;

import game.usn.bridge.api.protocol.AbstractUSNProtocol;
import game.usn.bridge.api.proxy.AbstractDataProxy;
import platform.service.api.IServiceProxy;

public abstract class AbstractPlatformClientProxy extends AbstractDataProxy implements IServiceProxy
{
    protected AbstractPlatformClientProxy(AbstractUSNProtocol consumerProtocol)
    {
        super(consumerProtocol);
    }
}
