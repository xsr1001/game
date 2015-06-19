/**
 * @file ServiceDiscoveryListener.java
 * @brief <description>
 */

package game.usn.sd.listener;

import java.net.Inet4Address;

public interface IServiceDiscoveryListener
{
    void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName, Integer shardId,
        Integer groupId, String serviceId);
}
