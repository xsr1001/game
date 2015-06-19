/**
 * @file USNSDManagerTest.java
 * @brief <description>
 */

package game.usn.sd.manager;

import game.core.api.exception.USNException;
import game.usn.sd.endpoint.IUSNEndpoint;
import game.usn.sd.environment.IEnvironmentManager;
import game.usn.sd.listener.IServiceDiscoveryListener;

import java.net.Inet4Address;
import java.util.UUID;

public class USNSDManagerTest
{
    public static void main(String[] args) throws Exception
    {

        USNSDManager.getInstance(new IEnvironmentManager() {

            @Override
            public void validateSDEndpointType(String endpointType) throws USNException
            {
                // TODO Auto-generated method stub

            }

            @Override
            public String getSDEndpointType(IUSNEndpoint endpoint) throws USNException
            {
                return "_admin";
            }

            @Override
            public String getEnvironmentId() throws USNException
            {
                return "dev";
            }

            @Override
            public String getDomain()
            {
                return "local";
            }
        });

        USNSDManager.getInstance(null).register(1337, "test_name", 5, 5, UUID.randomUUID(), new IUSNEndpoint() {

            @Override
            public String getEndpointType()
            {
                return "_admin";
            }
        }, null);

        USNSDManager.getInstance(null).browse("_admin", 5, 5, new IServiceDiscoveryListener() {

            @Override
            public void serviceResolved(Inet4Address[] hostIPv4List, int hostPort, String serviceName, Integer shardId,
                Integer groupId, String serviceId)
            {
                System.out.println("hue");

            }
        });
    }
}
