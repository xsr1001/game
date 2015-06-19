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
import game.usn.sd.listener.IServiceDiscoveryListener;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
public final class USNSDManager implements ServiceListener
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(USNSDManager.class);

    // Errors, args, messages.
    private static final String ERROR_IO_EXCEPTION = "I/O error received while using DNSSD controller.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_USN_EXCEPTION = "USN exception thrown from environment manager.";
    private static final String ERROR_NFE = "Number format exception thrown while converting shard/group id.";
    private static final String WARN_LISTENER_EXCEPTION = "Listener exception raised while being notified.";
    private static final String ARG_ENDPOINT_NETWORK_PORT = "endpointNetworkPort";
    private static final String ARG_ENDPOINT_NAME = "endpointName";
    private static final String ARG_ENDPOINT_SHARD_ID = "shardId";
    private static final String ARG_ENDPOINT_GROUP_ID = "groupId";
    private static final String ARG_ENDPOINT_ID = "endpointId";
    private static final String ARG_USN_EDNPOINT = "endpoint";
    private static final String ARG_ENDPOINT_SERVICE_TYPE = "endpointServiceType";
    private static final String ARG_SERVICE_DISCOVERY_LISTENER = "serviceDiscoveryListener";
    private static final String ARG_ENDPOINT_PROPS_MAP = "endpointPropertiesMap";
    private static final String ARG_ENVIRONMENT_MANAGER = "environmentManager";
    private static final String MSG_REGISTERING_ENDPOINT = "Registering new end-point ot USN: [%s]";
    private static final String MSG_BROWSE = "Browsing for USN end-point with type: [%s], shardId: [%d], groupId: [%d].";
    private static final String MSG_STOP_BROWSE = "Stopping browsing for USN end-point with type: [%s], shardId: [%d], groupId: [%d].";
    private static final String MSG_ALREADY_BROWSING = "Already browsing for USN end-point with type: [%s], shardId: [%d], groupId: [%d].";
    private static final String MSG_SERVICE_ADDED = "Service: [%s --> %s] has been added.";
    private static final String MSG_SERVICE_REMOVED = "Service: [%s --> %s] has been removed.";
    private static final String MSG_SERVICE_RESOLVED = "Service: [%s] has been resolved. Notifying listeners.";
    private static final String MSG_NO_LISTENERS = "No listeners present for service type: [%s], shard id: [%d], group id: [%d].";

    // Optional service map info keys and other service type keys.
    private static final String KEY_GROUP_ID = "gId";
    private static final String KEY_SERVICE_ID = "sId";
    private static final String KEY_SERVICE_PROTOCOL_TCP = "_tcp";
    private static final String KEY_DOT_DELIMETER = ".";

    // Default group and shard id.
    private static final int DEFAULT_SHARD_ID = -1;
    private static final int DEFAULT_GROUP_ID = -1;

    // Singleton instance.
    private static USNSDManager instance;

    // Determines if USNSDManager is initialized.
    private AtomicBoolean initialized;

    // Service discovery controller.
    public JmDNS sdController;

    // User/domain specific environment manager.
    IEnvironmentManager environmentManager;

    // Browse map. Maps service type to shard id to group id to list of service listeners.
    private Map<String, Map<Integer, Map<Integer, Set<IServiceDiscoveryListener>>>> browseMap;

    // Browse map synchronization.
    private ReentrantReadWriteLock rwLockBrowseMap;

    /**
     * Singleton getter.
     * 
     * @param environmentManager
     *            - implementation of {@link IEnvironmentManager}. Implementation may be specific to user desired
     *            security settings and/or domain constraints. This parameter is only required on initial instantiation.
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

        this.browseMap = new HashMap<String, Map<Integer, Map<Integer, Set<IServiceDiscoveryListener>>>>();
        this.rwLockBrowseMap = new ReentrantReadWriteLock();
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
     * Attempts to register an end-point on Unified Service Network. A fully qualified USN end-point is composed as:
     * [<shardId>._sub.<endpointServiceType>._tcp.<environmentDomain>.<domain>.] A fully qualified USN end-point MUST
     * provide a specific end-point name and a valid USN shard id. Optionally it MAY provide USN group id and additional
     * properties map.
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

            // Create service info and register.
            ServiceInfo serviceEntry = ServiceInfo.create(
                constructServiceType(this.environmentManager.getSDEndpointType(endpoint), KEY_SERVICE_PROTOCOL_TCP,
                    this.environmentManager.getEnvironmentId(), this.environmentManager.getDomain()), endpointName,
                shardId.toString(), endpointNetworkPort, 0, 0, servicePropertyMap);

            LOG.info(String.format(MSG_REGISTERING_ENDPOINT, serviceEntry.toString()));
            this.sdController.registerService(serviceEntry);
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

    /**
     * Browse for an end-point on USN.
     * 
     * @param endpointServiceType
     *            - an {@link String} environment valid end-point service type. This must be one of the types returned
     *            from {@link IEnvironmentManager#getSDEndpointType(IUSNEndpoint)} call.
     * @param shardId
     *            - an {@link Integer} environment valid shard id. Optional parameter to limit the search to set of
     *            end-points determined by shard id.
     * @param groupId
     *            - an {@link Integer} valid group id. Optional parameter to limit the search to set of end-points
     *            determined by group id.
     * @param serviceDiscoveryListener
     *            - an implementation of {@link IServiceDiscoveryListener} providing browse results callback.
     * @throws USNException
     *             - throw a {@link USNException} on browse error.
     */
    public void browse(String endpointServiceType, Integer shardId, Integer groupId,
        IServiceDiscoveryListener serviceDiscoveryListener) throws USNException
    {
        LOG.enterMethod(ARG_ENDPOINT_SERVICE_TYPE, endpointServiceType, ARG_ENDPOINT_SHARD_ID, shardId,
            ARG_ENDPOINT_GROUP_ID, groupId, ARG_SERVICE_DISCOVERY_LISTENER, serviceDiscoveryListener);
        try
        {
            ArgsChecker.errorOnNull(endpointServiceType, ARG_ENDPOINT_SERVICE_TYPE);
            ArgsChecker.errorOnNull(serviceDiscoveryListener, ARG_SERVICE_DISCOVERY_LISTENER);
            this.environmentManager.validateSDEndpointType(endpointServiceType);

            doSanityCheck();

            // Default browse shard id is -1.
            if (shardId == null)
            {
                shardId = new Integer(DEFAULT_SHARD_ID);
            }

            // Default browse group id is -1.
            if (groupId == null)
            {
                groupId = new Integer(DEFAULT_GROUP_ID);
            }

            if (!addBrowseEntry(endpointServiceType, shardId, groupId, serviceDiscoveryListener))
            {
                LOG.info(String.format(MSG_ALREADY_BROWSING, endpointServiceType, shardId, groupId));
                return;
            }

            LOG.info(String.format(MSG_BROWSE, endpointServiceType, shardId, groupId));

            // Browse.
            this.sdController.addServiceListener(
                constructServiceType(endpointServiceType, KEY_SERVICE_PROTOCOL_TCP,
                    this.environmentManager.getEnvironmentId(), this.environmentManager.getDomain()), this);
        }
        catch (IOException ioe)
        {
            LOG.error(ERROR_IO_EXCEPTION, ioe);
            throw new USNException(ERROR_IO_EXCEPTION, ioe);
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new USNException(ERROR_ILLEGAL_ARGUMENT, ie);
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

    public void browseStop(String endpointServiceType, Integer shardId, Integer groupId,
        IServiceDiscoveryListener serviceDiscoveryListener) throws USNException
    {
        LOG.enterMethod(ARG_ENDPOINT_SERVICE_TYPE, endpointServiceType, ARG_ENDPOINT_SHARD_ID, shardId,
            ARG_ENDPOINT_GROUP_ID, groupId, ARG_SERVICE_DISCOVERY_LISTENER, serviceDiscoveryListener);

        try
        {
            ArgsChecker.errorOnNull(endpointServiceType, ARG_ENDPOINT_SERVICE_TYPE);
            ArgsChecker.errorOnNull(serviceDiscoveryListener, ARG_SERVICE_DISCOVERY_LISTENER);

            doSanityCheck();

            // Default browse shard id is -1.
            if (shardId == null)
            {
                shardId = new Integer(DEFAULT_SHARD_ID);
            }

            // Default browse group id is -1.
            if (groupId == null)
            {
                groupId = new Integer(DEFAULT_GROUP_ID);
            }

            if (removeBrowseEntry(endpointServiceType, shardId, groupId, serviceDiscoveryListener))
            {
                LOG.info(String.format(MSG_STOP_BROWSE, endpointServiceType, shardId, groupId));
                try
                {
                    this.rwLockBrowseMap.readLock().lock();

                    // Nobody browsing for this type anymore, unbrowse it on controller.
                    if (!this.browseMap.containsKey(endpointServiceType))
                    {
                        this.sdController.removeServiceListener(
                            constructServiceType(endpointServiceType, KEY_SERVICE_PROTOCOL_TCP,
                                this.environmentManager.getEnvironmentId(), this.environmentManager.getDomain()), this);
                    }
                }
                finally
                {
                    this.rwLockBrowseMap.readLock().unlock();
                }
            }
        }
        catch (IOException ioe)
        {
            LOG.error(ERROR_IO_EXCEPTION, ioe);
            throw new USNException(ERROR_IO_EXCEPTION, ioe);
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new USNException(ERROR_ILLEGAL_ARGUMENT, ie);
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

    /**
     * Remove listener from browse map. Clean map structure as well if needed.
     * 
     * @param serviceType
     *            - a {@link String} application service type.
     * @param shardId
     *            - an {@link Integer} environment valid shard id.
     * @param groupId
     *            - an {@link Integer} valid group id.
     * @param serviceDiscoveryListener
     *            - an implementation of {@link IServiceDiscoveryListener} providing browse results callback.
     * @return true if entry was removed or false otherwise.
     */
    private boolean removeBrowseEntry(String serviceType, Integer shardId, Integer groupId,
        IServiceDiscoveryListener serviceDiscoveryListener)
    {
        try
        {
            this.rwLockBrowseMap.writeLock().lock();

            if (!this.browseMap.containsKey(serviceType) || !this.browseMap.get(serviceType).containsKey(shardId)
                || !this.browseMap.get(serviceType).get(shardId).containsKey(groupId)
                || !this.browseMap.get(serviceType).get(shardId).get(groupId).contains(serviceDiscoveryListener))
            {
                return false;
            }

            // Remove listener and empty structure.
            this.browseMap.get(serviceType).get(shardId).get(groupId).remove(serviceDiscoveryListener);

            if (this.browseMap.get(serviceType).get(shardId).get(groupId).size() == 0)
            {
                this.browseMap.get(serviceType).get(shardId).remove(groupId);

                if (this.browseMap.get(serviceType).get(shardId).size() == 0)
                {
                    this.browseMap.get(serviceType).remove(shardId);

                    if (this.browseMap.get(serviceType).size() == 0)
                    {
                        this.browseMap.remove(serviceType);
                    }
                }
            }
            return true;
        }
        finally
        {
            this.rwLockBrowseMap.writeLock().unlock();
        }
    }

    /**
     * Attempts to add a new browse entry to browse map.
     * 
     * @param serviceType
     *            - a {@link String} application service type.
     * @param shardId
     *            - an {@link Integer} environment valid shard id.
     * @param groupId
     *            - an {@link Integer} valid group id.
     * @param serviceDiscoveryListener
     *            - an implementation of {@link IServiceDiscoveryListener} providing browse results callback.
     * @return true if entry was added or false otherwise.
     */
    private boolean addBrowseEntry(String serviceType, Integer shardId, Integer groupId,
        IServiceDiscoveryListener serviceDiscoveryListener)
    {
        try
        {
            // Assume the worst and pre create entire structure.
            Set<IServiceDiscoveryListener> listenerSet = new HashSet<IServiceDiscoveryListener>();
            HashMap<Integer, Set<IServiceDiscoveryListener>> groupToListenerSetMap = new HashMap<Integer, Set<IServiceDiscoveryListener>>();
            Map<Integer, Map<Integer, Set<IServiceDiscoveryListener>>> shardToGroupMap = new HashMap<Integer, Map<Integer, Set<IServiceDiscoveryListener>>>();

            this.rwLockBrowseMap.writeLock().lock();

            if (!this.browseMap.containsKey(serviceType))
            {
                listenerSet.add(serviceDiscoveryListener);
                groupToListenerSetMap.put(groupId, listenerSet);
                shardToGroupMap.put(shardId, groupToListenerSetMap);

                this.browseMap.put(serviceType, shardToGroupMap);
                return true;
            }
            if (!this.browseMap.get(serviceType).containsKey(shardId))
            {
                listenerSet.add(serviceDiscoveryListener);
                groupToListenerSetMap.put(groupId, listenerSet);

                this.browseMap.get(serviceType).put(shardId, groupToListenerSetMap);
                return true;
            }

            if (!this.browseMap.get(serviceType).get(shardId).containsKey(groupId))
            {
                listenerSet.add(serviceDiscoveryListener);

                this.browseMap.get(serviceType).get(shardId).put(groupId, listenerSet);
                return true;
            }

            if (!this.browseMap.get(serviceType).get(shardId).get(groupId).contains(serviceDiscoveryListener))
            {
                this.browseMap.get(serviceType).get(shardId).get(groupId).add(serviceDiscoveryListener);
                return true;
            }

            return false;
        }
        finally
        {
            this.rwLockBrowseMap.writeLock().unlock();
        }
    }

    /**
     * Helper method for constructing USN DNSSD valid full service type.
     * 
     * @param serviceType
     *            - a {@link String} application service type.
     * @param protocol
     *            - a {@link String} application protocol.
     * @param subDomain
     *            - a {@link String} sub-domain.
     * @param domain
     *            - a {@link String} domain.
     * @return - a {@link String} full service type in format: [_<serviceType>._<protocol>.<subDomain>.<domain>.]
     */
    private String constructServiceType(String serviceType, String protocol, String subDomain, String domain)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceType).append(KEY_DOT_DELIMETER);
        sb.append(protocol).append(KEY_DOT_DELIMETER);
        sb.append(subDomain).append(KEY_DOT_DELIMETER);
        sb.append(domain).append(KEY_DOT_DELIMETER);

        return sb.toString();
    }

    /**
     * Notify listeners for provided service info.
     * 
     * @param serviceInfo
     *            - a {@link ServiceInfo} resolved service data.
     */
    private void notifyListeners(ServiceInfo serviceInfo)
    {
        LOG.enterMethod();
        try
        {
            this.rwLockBrowseMap.readLock().lock();

            // Extract service data.
            String serviceType = serviceInfo.getApplication();
            serviceType = "_".concat(serviceType);
            Integer shardId = Integer.valueOf(serviceInfo.getSubtype());
            Integer groupId = Integer.valueOf(serviceInfo.getPropertyString(KEY_GROUP_ID));
            String serviceId = serviceInfo.getPropertyString(KEY_SERVICE_ID);
            String serviceName = serviceInfo.getName();
            Inet4Address[] hostIPv4List = serviceInfo.getInet4Addresses();
            int hostPort = serviceInfo.getPort();

            // Create target listener set to notify. Includes listeners for all shard/group ids, and specific
            // shard/group ids.
            HashSet<IServiceDiscoveryListener> targetListenerSet = new HashSet<IServiceDiscoveryListener>();

            // Check specific shard listeners (concrete shard id).
            if (!this.browseMap.containsKey(serviceType) || !this.browseMap.get(serviceType).containsKey(shardId)
                || !this.browseMap.get(serviceType).get(shardId).containsKey(groupId))
            {
                LOG.info(String.format(MSG_NO_LISTENERS, serviceType, shardId, groupId));
            }
            else
            {
                targetListenerSet.addAll(this.browseMap.get(serviceType).get(shardId).get(groupId));
            }

            // Check specific listeners (concrete shard id).
            if (!this.browseMap.containsKey(serviceType)
                || !this.browseMap.get(serviceType).containsKey(DEFAULT_SHARD_ID)
                || !this.browseMap.get(serviceType).get(DEFAULT_SHARD_ID).containsKey(groupId))
            {
                LOG.info(String.format(MSG_NO_LISTENERS, serviceType, DEFAULT_SHARD_ID, groupId));
            }
            else
            {
                targetListenerSet.addAll(this.browseMap.get(serviceType).get(DEFAULT_SHARD_ID).get(groupId));
            }

            // Check general listeners (both shard id and group id 0).
            if (!this.browseMap.containsKey(serviceType)
                || !this.browseMap.get(serviceType).containsKey(DEFAULT_SHARD_ID)
                || !this.browseMap.get(serviceType).get(DEFAULT_SHARD_ID).containsKey(DEFAULT_GROUP_ID))
            {
                LOG.info(String.format(MSG_NO_LISTENERS, serviceType, DEFAULT_SHARD_ID, DEFAULT_GROUP_ID));
            }
            else
            {
                targetListenerSet.addAll(this.browseMap.get(serviceType).get(DEFAULT_SHARD_ID).get(DEFAULT_GROUP_ID));
            }

            // Guard against listener errors.
            try
            {
                for (IServiceDiscoveryListener listener : targetListenerSet)
                {
                    listener.serviceResolved(hostIPv4List, hostPort, serviceName, shardId, groupId, serviceId);
                }
            }
            catch (Exception e)
            {
                LOG.warn(WARN_LISTENER_EXCEPTION, e);
            }

        }
        catch (NumberFormatException nfe)
        {
            LOG.error(ERROR_NFE, nfe);
        }
        finally
        {
            this.rwLockBrowseMap.readLock().unlock();
            LOG.exitMethod();
        }
    }

    @Override
    public void serviceAdded(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_ADDED, event.getName(), event.getType()));
        sdController.requestServiceInfo(event.getType(), event.getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_REMOVED, event.getName(), event.getType()));
    }

    @Override
    public void serviceResolved(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_RESOLVED, event.getInfo()));
        notifyListeners(event.getInfo());
    }
}
