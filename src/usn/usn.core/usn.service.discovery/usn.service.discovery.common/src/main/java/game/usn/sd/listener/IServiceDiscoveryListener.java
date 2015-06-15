/**
 * @file ServiceDiscoveryListener.java
 * @brief <description>
 */

package game.usn.sd.listener;

public interface IServiceDiscoveryListener
{
    void serviceResolved(String hostIPv4, int hostPort);
}
