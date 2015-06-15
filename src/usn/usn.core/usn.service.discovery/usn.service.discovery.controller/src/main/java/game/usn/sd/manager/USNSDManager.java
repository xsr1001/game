/**
 * @file USNSDManager.java
 * @brief Unified Service Network service discovery manager.
 */

package game.usn.sd.manager;

import game.core.api.exception.USNException;
import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;
import game.core.util.NetworkUtils;
import game.usn.sd.endpoint.IUSNEndpoint;
import game.usn.sd.environment.IEnvironmentManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * Unified Service Network service discovery manager. Provides functionality for registering and browsing for other USN
 * end-points.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class USNSDManager
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(USNSDManager.class);

    // Errors, args, messages.
    private static final String ARG_ENDPOINT_NETWORK_PORT = "endpointNetworkPort";
    private static final String ARG_ENDPOINT_NAME = "endpointName";
    private static final String ARG_ENDPOINT_SHARD_ID = "shardId";
    private static final String ARG_ENDPOINT_GROUP_ID = "groupId";
    private static final String ARG_ENDPOINT_ID = "endpointId";
    private static final String ARG_USN_EDNPOINT = "endpoint";
    private static final String ARG_ENDPOINT_PROPS_MAP = "endpointPropertiesMap";
    private static final String ARG_ENVIRONMENT_MANAGER = "environmentManager";
    private static final String ERROR_IO_EXCEPTION = "I/O error received while using DNSSD controller.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String MSG_REGISTERING_ENDPOINT = "Registering new end-point ot USN: [%s]";

    // Optional service map info keys and other service type keys.
    private static final String KEY_GROUP_ID = "gId";
    private static final String KEY_SERVICE_ID = "sId";
    private static final String KEY_SERVICE_PROTOCOL_TCP = "_tcp";
    private static final String KEY_DOT_DELIMETER = ".";

    // Singleton instance.
    private static USNSDManager instance;

    // Determines if USNSDManager is initialized.
    private AtomicBoolean initialized;

    private static final String ERROR_USN_EXCEPTION = "USN exception thrown from environment manager.";

    // Service discovery controller.
    private JmDNS sdController;

    // User/domain specific environment manager.
    IEnvironmentManager environmentManager;

    /**
     * Singleton getter.
     * 
     * @param environmentManager
     *            - implementation of {@link IEnvironmentManager}. Implementation may be specific to user desired
     *            security setting and/or domain constraints. This parameter is only required on initial instantiation.
     *            It can be null on all subsequent calls.
     * @return - the only instance of {@link USNSDManager}.
     * @throws USNException
     *             - throw a {@link USNException} on invalid {@link IEnvironmentManager} instance.
     */
    public synchronized static USNSDManager getInstance(IEnvironmentManager environmentManager) throws USNException
    {
        LOG.enterMethod(ARG_ENVIRONMENT_MANAGER, environmentManager);
        try
        {
            if (instance != null)
            {
                return instance;
            }
            ArgsChecker.errorOnNull(environmentManager, ARG_ENVIRONMENT_MANAGER);
            instance = new USNSDManager(environmentManager);

            return instance;
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new USNException(ERROR_ILLEGAL_ARGUMENT, ie);
        }
        finally
        {

            LOG.exitMethod();
        }
    }

    /**
     * Private constructor.
     * 
     * @param environmentManager
     *            - implementation of {@link IEnvironmentManager}. Implementation may be specific to user desired
     *            security setting and/or domain constraints.
     */
    private USNSDManager(IEnvironmentManager environmentManager)
    {
        this.initialized = new AtomicBoolean(false);
        this.environmentManager = environmentManager;
    }

    /**
     * Performs self initialization and additional setup checks.
     * 
     * @throws IOException
     *             - throws {@link IOException} on {@link JmDNS} initialization error.
     */
    private synchronized void doSanityCheck() throws IOException
    {
        LOG.enterMethod();

        if (!this.initialized.get())
        {
            this.sdController = JmDNS.create();
            this.initialized.set(true);
        }

        LOG.exitMethod();
    }

    /**
     * Attempts to register an end-point on Unified Service Network. A fully qualified USN entry is composed as:
     * [<shardId>._sub.<endpointServiceType>._tcp.<environmentDomain>.<domain>.] A fully qualified USN entry MUST
     * provide a user specific end-point name and a valid USN shard id. Optionally it MAY provide USN group id and
     * additional properties map.
     * 
     * @param endpointNetworkPort
     *            - network port which end-point provides its service on. End-point must be actively accepting new
     *            connections on provided network port.
     * @param endpointName
     *            - a {@link String} end-point name. This is NOT a unique identifier for provided end-point. DNSSD
     *            controller may change end-point name if case another end-point with same name already exists in
     *            environment domain.
     * @param shardId
     *            - a {@link Integer} USN valid shard id. This is a unique identifier for a set of end-points that
     *            provide a common set of capabilities (e.g. shard id can be unique for all end-points that offer
     *            capabilities for a specific game zone instance). ShardId is a USN required parameter and is unique per
     *            environment.
     * @param groupId
     *            - a {@link Integer} USN valid group id. Represents a subset of specific shard end-point set and
     *            represents a logically dependent set of end-points. (e.g. group id represents N replicated end-points
     *            that provide the same functionality from different physical nodes - replicated combat service in
     *            combat intensive area). GroupId is a USN optional parameter and is unique per shard. Defaults to -1.
     * @param endpointId
     *            - a {@link UUID} environment unique end-point id. End-point id is a USN required parameter and is
     *            unique per environment.
     * @param endpoint
     *            - an implementation of {@link IUSNEndpoint}. Represents an instance of USN end-point capable of
     *            providing service to other end-points.
     * @param endpointPropertiesMap
     *            - a {@link Map}<{@link String}, {@link String}> optional property map. End-point MAY provide
     *            additional parameters to USN.
     * @throws USNException
     *             - throw {@link USNException} on data validation error or actual end-point registration error.
     */
    public synchronized void register(int endpointNetworkPort, String endpointName, Integer shardId, Integer groupId,
        final UUID endpointId, final IUSNEndpoint endpoint, Map<String, String> endpointPropertiesMap)
        throws USNException
    {
        LOG.enterMethod(ARG_ENDPOINT_NETWORK_PORT, endpointNetworkPort, ARG_ENDPOINT_NAME, endpointName,
            ARG_ENDPOINT_SHARD_ID, shardId, ARG_ENDPOINT_GROUP_ID, groupId, ARG_ENDPOINT_ID, endpointId,
            ARG_USN_EDNPOINT, endpoint, ARG_ENDPOINT_PROPS_MAP, endpointPropertiesMap);

        try
        {
            ArgsChecker.errorOnNull(endpointName, ARG_ENDPOINT_NAME);
            ArgsChecker.errorOnNull(endpointId, ARG_ENDPOINT_ID);
            ArgsChecker.errorOnNull(endpoint, ARG_USN_EDNPOINT);
            ArgsChecker.errorOnNull(shardId, ARG_ENDPOINT_SHARD_ID);
            ArgsChecker.errorOnLessThan0(shardId.intValue(), ARG_ENDPOINT_SHARD_ID);
            NetworkUtils.validateNetworkPort(endpointNetworkPort);

            // Sanity check for self initialization.
            doSanityCheck();

            // Group id is optional, defaults to -1.
            if (groupId == null)
            {
                groupId = new Integer(-1);
            }

            // Create service properties map.
            Map<String, String> servicePropertyMap = new HashMap<String, String>();
            servicePropertyMap.put(KEY_GROUP_ID, groupId.toString());
            servicePropertyMap.put(KEY_SERVICE_ID, endpointId.toString());
            if (endpointPropertiesMap != null)
            {
                servicePropertyMap.putAll(servicePropertyMap);
            }

            // Construct fully qualified service type.
            StringBuilder sb = new StringBuilder();
            sb.append(this.environmentManager.getSDEndpointType(endpoint)).append(KEY_DOT_DELIMETER);
            sb.append(KEY_SERVICE_PROTOCOL_TCP).append(KEY_DOT_DELIMETER);
            sb.append(this.environmentManager.getEnvironmentId(endpoint)).append(KEY_DOT_DELIMETER);
            sb.append(this.environmentManager.getDomain()).append(KEY_DOT_DELIMETER);

            ServiceInfo serviceEnty = ServiceInfo.create(sb.toString(), endpointName, shardId.toString(),
                endpointNetworkPort, 0, 0, servicePropertyMap);

            LOG.info(String.format(MSG_REGISTERING_ENDPOINT, serviceEnty.toString()));
            this.sdController.registerService(serviceEnty);
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new USNException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        catch (IOException ioe)
        {
            LOG.error(ERROR_IO_EXCEPTION, ioe);
            throw new USNException(ERROR_IO_EXCEPTION, ioe);
        }
        catch (USNException use)
        {
            LOG.error(ERROR_USN_EXCEPTION, use);
            throw use;
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    public static void doBrowse()
    {
        try
        {
            final JmDNS jmdns = JmDNS.create();
            jmdns.addServiceListener("_admin._tcp.dev.local.", new SampleListener());
        }
        catch (Exception ioe)
        {
            System.err.println(ioe);
        }
    }

    static class SampleListener implements ServiceListener
    {
        @Override
        public void serviceAdded(ServiceEvent event)
        {
            try
            {
                System.out.println("Service added   : " + event.getName() + "." + event.getType());
                JmDNS jmdns = JmDNS.create();
                jmdns.requestServiceInfo("_admin._tcp.dev.game.", event.getName());
            }
            catch (Exception ioe)
            {
                System.err.println(ioe);
            }
        }

        @Override
        public void serviceRemoved(ServiceEvent event)
        {
            System.out.println("Service removed : " + event.getName() + "." + event.getType());
        }

        @Override
        public void serviceResolved(ServiceEvent event)
        {
            System.out.println("Service resolved: " + event.getInfo());
        }
    }
}
