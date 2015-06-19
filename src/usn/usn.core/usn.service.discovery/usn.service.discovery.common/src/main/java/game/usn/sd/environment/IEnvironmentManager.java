/**
 * @file IEnvironmentManagerProxy.java
 * @brief <description>
 */

package game.usn.sd.environment;

import game.core.api.exception.USNException;
import game.usn.sd.endpoint.IUSNEndpoint;

public interface IEnvironmentManager
{
    public String getEnvironmentId() throws USNException;

    public String getDomain();

    public String getSDEndpointType(IUSNEndpoint endpoint) throws USNException;

    public void validateSDEndpointType(String endpointType) throws USNException;
}
