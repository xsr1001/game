/**
 * @file PlatformSDManager.java
 * @brief Platform service discovery manager.
 */

package platform.dnssd.manager;

import game.core.api.exception.PlatformException;
import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import platform.dnssd.api.IPlatformSDContextManager;
import platform.dnssd.api.endpoint.ISDEntity;
import platform.dnssd.api.filter.ISDResultFilter;
import platform.dnssd.api.filter.ISDSingleResultFilter;
import platform.dnssd.api.filter.ServiceBrowseResult;
import platform.dnssd.api.listener.ISDListener;
import platform.dnssd.listener.ISDBrowseResultListener;

/**
 * Platform service discovery manager. Provides functionality for browsing and advertising entities via multicast DNS on
 * given platform instance.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformSDManager implements ISDBrowseResultListener
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(PlatformSDManager.class);

    // Errors, args, messages.
    private static final String ERROR_IO_EXCEPTION = "I/O error received while using JmDNS manager.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_INITIALIZED = "Sevice discovery manager has not been initialized yet.";
    private static final String WARN_LISTENER_EXCEPTION = "Listener exception raised while being notified with resolved service data.";
    private static final String ARG_PLATFORM_CONTEXT_MANAGER = "platformSDContextManager";
    private static final String ARG_BROWSE_LISTENER = "browseResultListener";
    private static final String ARG_BROWSE_RESULT_FILTER = "browseResultFilter";
    private static final String ARG_ENTITY_NETWORK_PORT = "sdEntityNetworkPort";
    private static final String ARG_ENTITY_NAME = "sdEntityName";
    private static final String ARG_ENTITY = "sdEntity";
    private static final String ARG_ENTITY_CONTEXT = "sdEntityContext";
    private static final String MSG_ADVERTISING = "Advertising new service discovery entity: [%s] on current platform.";
    private static final String MSG_ALREADY_ADVERTISING = "Service discovery entity with type: [%s] is already being advertised.";
    private static final String ARG_SERVICE_TYPE = "serviceType";

    // Singleton instance.
    private static final PlatformSDManager INSTANCE = new PlatformSDManager();

    // Determines if PlatformSDManager is initialized.
    private AtomicBoolean initialized;

    // Browse and advertise managers.
    private SDManagerBrowse sdManagerBrowse;
    private SDManagerAdvertise sdManagerAdvertise;

    // Service discovery controller.
    public JmDNS jmDNSManager;

    // Platform service discovery context manager.
    private IPlatformSDContextManager platformSDContextManager;

    // Service type keys.
    private static final String DEFAULT_PROTOCOL_TCP = "tcp";
    private static final String KEY_UNDERSCORE = "_";
    private static final String KEY_DOT_DELIMETER = ".";

    // Synchronization mechanisms.
    private ReentrantReadWriteLock advertiseRWLock;
    private ReentrantReadWriteLock serviceCacheRWLock;
    private ReentrantReadWriteLock browseRWLock;

    // Advertise map. Maps service discovery entity type to ServiceInfo.
    private Map<String, ServiceInfo> advertiseMap;

    // Service cache map. Map service type to list of resolved services.
    private Map<String, List<ServiceBrowseResult>> serviceCacheMap;

    // Browse listener map. Map service type to a set of listeners listening for resolved services of this type.
    private Map<String, Set<ISDListener>> browseListenerMap;

    // Listener filter map. Maps listener to service type to result filter.
    private Map<ISDListener, Map<String, ISDResultFilter>> listenerFilterMap;

    /**
     * Singleton getter.
     * 
     * @return - return singleton instance of {@link USNSDManager}.
     */
    public synchronized static PlatformSDManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private PlatformSDManager()
    {
        initialized = new AtomicBoolean(false);

        advertiseMap = new HashMap<String, ServiceInfo>();
        serviceCacheMap = new HashMap<String, List<ServiceBrowseResult>>();
        browseListenerMap = new HashMap<String, Set<ISDListener>>();
        listenerFilterMap = new HashMap<ISDListener, Map<String, ISDResultFilter>>();

        advertiseRWLock = new ReentrantReadWriteLock();
        serviceCacheRWLock = new ReentrantReadWriteLock();
        browseRWLock = new ReentrantReadWriteLock();
    }

    /**
     * Initialize platform SD manager with SD context manager.
     * 
     * @param platformSDContextManager
     *            - an instance of {@link IPlatformSDContextManager} defining context for service discovery.
     * @throws PlatformException
     *             - throw {@link PlatformException} on initialization error.
     */
    public synchronized void init(IPlatformSDContextManager platformSDContextManager) throws PlatformException
    {
        LOG.enterMethod();
        try
        {
            ArgsChecker.errorOnNull(platformSDContextManager, ARG_PLATFORM_CONTEXT_MANAGER);
            if (!initialized.get())
            {
                this.platformSDContextManager = platformSDContextManager;

                jmDNSManager = JmDNS.create();
                sdManagerBrowse = new SDManagerBrowse(jmDNSManager, this);
                sdManagerAdvertise = new SDManagerAdvertise(jmDNSManager);

                initialized.set(true);
            }
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new PlatformException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        catch (IOException ioe)
        {
            LOG.error(ERROR_IO_EXCEPTION, ioe);
            throw new PlatformException(ERROR_IO_EXCEPTION, ioe);
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    /**
     * Shutdown service discovery manager and cleanup.
     */
    public synchronized void shutdown()
    {
        LOG.enterMethod();

        initialized.set(false);
        sdManagerBrowse.shutdown();
        sdManagerAdvertise.shutdown();

        browseListenerMap.clear();
        listenerFilterMap.clear();
        advertiseMap.clear();
        serviceCacheMap.clear();

        LOG.exitMethod();
    }

    /**
     * Advertise new service discovery entity on given platform instance.
     * 
     * @param sdEntity
     *            - a source {@link ISDEntity} to advertise.
     * @param sdEntityName
     *            - a {@link String} sd entity name. Name may be changed by underlying manager to preserve uniqueness.
     * @param sdEntityNetworkPort
     *            - a valid network port which sd entity is listening on.
     * @param sdEntityContext
     *            - a {@link Map}<{@link String},{@link String}> map providing custom sd entity context to advertise the
     *            entity with.
     * @return true if service discovery entity has been successfully advertised or false otherwise.
     * @throws PlatformException
     *             - throw {@link PlatformException} on error.
     */
    public synchronized boolean advertise(final ISDEntity sdEntity, final String sdEntityName, int sdEntityNetworkPort,
        final Map<String, String> sdEntityContext) throws PlatformException
    {
        LOG.enterMethod(ARG_ENTITY, sdEntity, ARG_ENTITY_NAME, sdEntityName, ARG_ENTITY_NETWORK_PORT,
            sdEntityNetworkPort, ARG_ENTITY_CONTEXT, sdEntityContext);

        if (!initialized.get())
        {
            throw new PlatformException(ERROR_INITIALIZED);
        }

        try
        {
            ArgsChecker.errorOnNull(sdEntity, ARG_ENTITY);
            ArgsChecker.errorOnNull(sdEntityName, ARG_ENTITY_NAME);

            if (isAdvertised(sdEntity))
            {
                LOG.info(String.format(MSG_ALREADY_ADVERTISING, sdEntity));
                return false;
            }

            Map<String, String> propertyMap = new HashMap<String, String>();
            if (sdEntityContext != null)
            {
                propertyMap.putAll(sdEntityContext);
            }

            // Create service info and register.
            ServiceInfo serviceInfo = ServiceInfo.create(
                constructServiceType(sdEntity.getEntityServiceType(), DEFAULT_PROTOCOL_TCP,
                    platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()), sdEntityName,
                sdEntityNetworkPort, 0, 0, true, propertyMap);

            LOG.info(String.format(MSG_ADVERTISING, serviceInfo.toString()));
            jmDNSManager.registerService(serviceInfo);

            try
            {
                advertiseRWLock.writeLock().lock();
                advertiseMap.put(sdEntity.getEntityServiceType(), serviceInfo);
            }
            finally
            {
                advertiseRWLock.writeLock().unlock();
            }

            return true;
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new PlatformException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        catch (IOException ioe)
        {
            LOG.error(ERROR_IO_EXCEPTION, ioe);
            throw new PlatformException(ERROR_IO_EXCEPTION, ioe);
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    /**
     * Stops advertising service discovery entity.
     * 
     * @param sdEntity
     *            - a source {@link ISDEntity} to stop advertising.
     * @throws PlatformException
     *             - throw {@link PlatformException} on error.
     */
    public synchronized void advertiseStop(ISDEntity sdEntity) throws PlatformException
    {
        LOG.enterMethod(ARG_ENTITY, sdEntity);

        if (!initialized.get())
        {
            throw new PlatformException(ERROR_INITIALIZED);
        }

        try
        {
            ArgsChecker.errorOnNull(sdEntity, ARG_ENTITY);

            if (!isAdvertised(sdEntity))
            {
                return;
            }

            ServiceInfo serviceInfo = advertiseMap.get(sdEntity.getEntityServiceType());
            jmDNSManager.unregisterService(serviceInfo);

            try
            {
                advertiseRWLock.writeLock().lock();
                advertiseMap.remove(sdEntity.getEntityServiceType());
            }
            finally
            {
                advertiseRWLock.writeLock().unlock();
            }
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new PlatformException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    /**
     * Start browsing for a service.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} instance to notify resolved service data to.
     * @param serviceType
     *            - a {@link String} service type to browse for.
     * @param browseResultFilter
     *            - an implementation of {@link ISDResultFilter} to filter received results. Optional. Caller should
     *            provide necessary business logic for filtering results. If null, all results will be passed via
     *            provided callback.
     * @throws PlatformException
     *             - throw {@link PlatformException} on browse error.
     */
    public void browse(ISDListener browseResultListener, String serviceType, ISDResultFilter browseResultFilter)
        throws PlatformException
    {
        LOG.enterMethod(ARG_BROWSE_LISTENER, browseResultListener, ARG_SERVICE_TYPE, serviceType,
            ARG_BROWSE_RESULT_FILTER, browseResultFilter);

        if (!initialized.get())
        {
            throw new PlatformException(ERROR_INITIALIZED);
        }
        try
        {
            ArgsChecker.errorOnNull(browseResultListener, ARG_BROWSE_LISTENER);
            ArgsChecker.errorOnNull(serviceType, ARG_SERVICE_TYPE);

            // Notify with cached data first.
            try
            {
                serviceCacheRWLock.readLock().lock();

                List<ServiceBrowseResult> filteredServiceBrowseList = null;
                if (browseResultFilter != null && serviceCacheMap.get(serviceType) != null)
                {
                    filteredServiceBrowseList = browseResultFilter.filter(serviceCacheMap.get(serviceType));
                }
                else
                {
                    filteredServiceBrowseList = serviceCacheMap.get(serviceType);
                }

                if (filteredServiceBrowseList != null && filteredServiceBrowseList.size() > 0)
                {
                    browseResultListener.serviceResolved(filteredServiceBrowseList);
                    if (browseResultFilter instanceof ISDSingleResultFilter)
                    {
                        return;
                    }
                }
            }
            finally
            {
                serviceCacheRWLock.readLock().unlock();
            }

            try
            {
                browseRWLock.writeLock().lock();

                if (addBrowseListener(browseResultListener, serviceType, browseResultFilter))
                {
                    sdManagerBrowse.browse(constructServiceType(serviceType, DEFAULT_PROTOCOL_TCP,
                        platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()));
                }
            }
            finally
            {
                browseRWLock.writeLock().unlock();
            }
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new PlatformException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    /**
     * Stop browsing for service type.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} instance browsing for given service type.
     * @param serviceType
     *            - a {@link String} service type a listener would like to stop browsing for.
     * @throws PlatformException
     *             - throw {@link PlatformException} on browse stop error.
     */
    public void browseStop(ISDListener browseResultListener, String serviceType) throws PlatformException
    {
        LOG.enterMethod(ARG_BROWSE_LISTENER, browseResultListener, ARG_SERVICE_TYPE, serviceType);

        if (!initialized.get())
        {
            throw new PlatformException(ERROR_INITIALIZED);
        }
        try
        {
            ArgsChecker.errorOnNull(browseResultListener, ARG_BROWSE_LISTENER);
            ArgsChecker.errorOnNull(serviceType, ARG_SERVICE_TYPE);

            try
            {
                browseRWLock.writeLock().lock();
                if (removeBrowseListener(browseResultListener, serviceType))
                {
                    sdManagerBrowse.browseStop(constructServiceType(serviceType, DEFAULT_PROTOCOL_TCP,
                        platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()));
                }
            }
            finally
            {
                browseRWLock.writeLock().unlock();
            }
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new PlatformException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    /**
     * Add a new browse result listener if not already present.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} instance to notify resolved service data to.
     * @param serviceType
     *            - a {@link String} service type listener is browsing for.
     * @param browseResultFilter
     *            - an implementation of {@link ISDResultFilter} to filter received results.
     */
    private boolean addBrowseListener(ISDListener browseResultListener, String serviceType,
        ISDResultFilter browseResultFilter)
    {
        boolean newAdded = false;

        // Create browse listener map structure if not already present.
        if (!browseListenerMap.containsKey(serviceType))
        {
            browseListenerMap.put(serviceType, new HashSet<ISDListener>());
        }
        newAdded = browseListenerMap.get(serviceType).add(browseResultListener);

        // Create browse filter map structure if not already present.
        if (browseResultFilter != null)
        {
            if (!listenerFilterMap.containsKey(browseResultListener))
            {
                listenerFilterMap.put(browseResultListener, new HashMap<String, ISDResultFilter>());
            }
            listenerFilterMap.get(browseResultListener).put(serviceType, browseResultFilter);
        }

        return newAdded;
    }

    /**
     * Remove a browse result listener if present.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} instance to notify resolved service data to.
     * @param serviceType
     *            - a {@link String} service type listener is browsing for.
     */
    private boolean removeBrowseListener(ISDListener browseResultListener, String entityServiceType)
    {
        boolean removed = false;
        if (browseListenerMap.containsKey(entityServiceType))
        {
            removed = browseListenerMap.get(entityServiceType).remove(browseResultListener);
            if (browseListenerMap.get(entityServiceType).size() == 0)
            {
                browseListenerMap.remove(entityServiceType);
            }
        }

        if (listenerFilterMap.containsKey(browseResultListener))
        {
            listenerFilterMap.get(browseResultListener).remove(entityServiceType);
            if (listenerFilterMap.get(browseResultListener).size() == 0)
            {
                listenerFilterMap.remove(browseResultListener);
            }
        }

        return removed;
    }

    /**
     * Helper method for constructing JmDNS valid full service type.
     * 
     * @param serviceType
     *            a {@link String} application service type.
     * @param protocol
     *            a {@link String} application protocol.
     * @param subDomain
     *            a {@link String} sub-domain.
     * @param domain
     *            a {@link String} domain.
     * @return a {@link String} full service type in format: [_<serviceType>._<protocol>.<subDomain>.<domain>.]
     */
    private String constructServiceType(String serviceType, String protocol, String subDomain, String domain)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(serviceType).append(KEY_DOT_DELIMETER);
        sb.append(KEY_UNDERSCORE).append(protocol).append(KEY_DOT_DELIMETER);
        sb.append(subDomain).append(KEY_DOT_DELIMETER);
        sb.append(domain).append(KEY_DOT_DELIMETER);

        return sb.toString();
    }

    /**
     * Check if service discovery manager is browsing for given service type.
     * 
     * @param serviceType
     *            - a {@link String} service type to check if browsing for.
     * @return - true if browsing for given service type or false otherwise.
     */
    public boolean isBrowsing(String serviceType)
    {
        return sdManagerBrowse.isBrowsing(constructServiceType(serviceType, DEFAULT_PROTOCOL_TCP,
            platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()));
    }

    /**
     * Check whether or not given sd entity is already being advertised.
     * 
     * @param sdEntity
     *            - a source {@link ISDEntity}.
     * @return true if given sd entity is already being advertised.
     */
    public boolean isAdvertised(ISDEntity sdEntity)
    {
        LOG.enterMethod(ARG_ENTITY, sdEntity);
        try
        {
            advertiseRWLock.readLock().lock();
            return advertiseMap.containsKey(sdEntity.getEntityServiceType());
        }
        finally
        {
            advertiseRWLock.readLock().unlock();
            LOG.exitMethod();
        }
    }

    /**
     * Notify listener with resolved service results. If filter was provided when performing browse operation results
     * will be filtered.
     * 
     * @param sdListener
     *            - a {@link ISDListener} to notify with filtered results.
     * @param serviceResultList
     *            - a {@link List} of {@link ServiceBrowseResult} objects of that were resolved. This may contain cached
     *            results or freshly resolved ones.
     */
    private void notifyListener(ISDListener sdListener, List<ServiceBrowseResult> serviceResultList)
    {
        // Retrieve service type from underlying object.
        String serviceType = serviceResultList.get(0).getType();

        // Check if we are still browsing (filter still exists).
        ISDResultFilter resultFilter = null;
        try
        {
            browseRWLock.readLock().lock();
            if (listenerFilterMap.containsKey(sdListener))
            {
                resultFilter = listenerFilterMap.get(sdListener).get(serviceType);
            }
        }
        finally
        {
            browseRWLock.readLock().unlock();
        }

        // Filter.
        List<ServiceBrowseResult> filteredServiceResultList = null;
        if (resultFilter != null)
        {
            filteredServiceResultList = resultFilter.filter(serviceResultList);
        }
        else
        {
            filteredServiceResultList = serviceResultList;
        }

        // Notify and remove listener if single result filter was provided.
        if (filteredServiceResultList != null)
        {
            if (resultFilter instanceof ISDSingleResultFilter)
            {
                try
                {
                    browseRWLock.writeLock().lock();

                    removeBrowseListener(sdListener, serviceType);
                    sdManagerBrowse.browseStop(constructServiceType(serviceType, DEFAULT_PROTOCOL_TCP,
                        platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()));
                }
                finally
                {
                    browseRWLock.writeLock().unlock();
                }
            }

            // Notify listener and guard from listener exceptions.
            try
            {
                sdListener.serviceResolved(filteredServiceResultList);
            }
            catch (Exception e)
            {
                LOG.warn(WARN_LISTENER_EXCEPTION, e);
            }
        }
    }

    @Override
    public void notifyServiceResolved(ServiceBrowseResult browseResult)
    {
        // Add to cache.
        try
        {
            serviceCacheRWLock.writeLock().lock();
            if (!serviceCacheMap.containsKey(browseResult.getType()))
            {
                List<ServiceBrowseResult> cachedBrowseResultList = new ArrayList<ServiceBrowseResult>();
                serviceCacheMap.put(browseResult.getType(), cachedBrowseResultList);
            }
            serviceCacheMap.get(browseResult.getType()).add(browseResult);
        }
        finally
        {
            serviceCacheRWLock.writeLock().unlock();
        }

        // Notify listeners.
        if (browseListenerMap.containsKey(browseResult.getType()))
        {
            for (ISDListener listener : browseListenerMap.get(browseResult.getType()))
            {
                notifyListener(listener, Arrays.asList(new ServiceBrowseResult[] { browseResult }));
            }
        }
    }

    @Override
    public void notifyServiceRemoved(ServiceBrowseResult browseResult)
    {
        try
        {
            serviceCacheRWLock.writeLock().lock();
            if (serviceCacheMap.containsKey(browseResult.getType()))
            {
                serviceCacheMap.get(browseResult.getType()).remove(browseResult);
            }
        }
        finally
        {
            serviceCacheRWLock.writeLock().unlock();
        }
    }
}
