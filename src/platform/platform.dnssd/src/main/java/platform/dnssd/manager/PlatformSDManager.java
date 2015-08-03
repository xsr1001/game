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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import platform.dnssd.api.IPlatformSDContextManager;
import platform.dnssd.api.endpoint.ISDEntity;
import platform.dnssd.api.filter.ISDResultFilter;
import platform.dnssd.api.filter.ISDSingleResultFilter;
import platform.dnssd.api.filter.SDEntityBrowseResult;
import platform.dnssd.api.listener.ISDListener;

/**
 * Platform service discovery manager. Provides functionality for browsing and advertising entities via multicast DNS on
 * given platform instance.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformSDManager implements ServiceListener
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(PlatformSDManager.class);

    // Errors, args, messages.
    private static final String ERROR_IO_EXCEPTION = "I/O error received while using JmDNS manager.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_INITIALIZED = "Sevice discovery manager has not been initialized yet.";
    private static final String WARN_LISTENER_EXCEPTION = "Listener exception raised while being notified.";
    private static final String MSG_SERVICE_ADDED = "Service: [%s --> %s] has been added.";
    private static final String MSG_SERVICE_REMOVED = "Service: [%s --> %s] has been removed.";
    private static final String MSG_SERVICE_RESOLVED = "Service: [%s] has been resolved. Notifying listeners.";
    private static final String ARG_ENTITY_NETWORK_PORT = "sdEntityNetworkPort";
    private static final String ARG_ENTITY_NAME = "sdEntityName";
    private static final String ARG_ENTITY = "sdEntity";
    private static final String ARG_ENTITY_CONTEXT = "sdEntityContext";
    private static final String MSG_ADVERTISING = "Advertising new service discovery entity: [%s] on current platform.";
    private static final String MSG_ALREADY_ADVERTISING = "Service discovery entity with type: [%s] is already being advertised.";
    private static final String ARG_BROWSE_LISTENER = "browseResultListener";
    private static final String ARG_ENTITY_SERVICE_TYPE = "entityServiceType";
    private static final String ARG_BROWSE_RESULT_FILTER = "browseResultFilter";
    private static final String ARG_PLATFORM_CONTEXT_MANAGER = "platformSDContextManager";

    // Singleton instance.
    private static final PlatformSDManager INSTANCE = new PlatformSDManager();

    // Determines if PlatformSDManager is initialized.
    private AtomicBoolean initialized;

    // Service type keys.
    private static final String DEFAULT_PROTOCOL_TCP = "tcp";
    private static final String KEY_UNDERSCORE = "_";
    private static final String KEY_DOT_DELIMETER = ".";

    // Platform service discovery context manager.
    private IPlatformSDContextManager platformSDContextManager;

    // Service discovery controller.
    public JmDNS jmDNSManager;

    // Synchronization mechanisms.
    private ReentrantReadWriteLock advertiseRWLock;
    private ReentrantReadWriteLock serviceCacheRWLock;
    private ReentrantReadWriteLock browseRWLock;

    // Advertise map. Maps service discovery entity type to ServiceInfo.
    private Map<String, ServiceInfo> advertiseMap;

    // Service cache map. Map service discovery entity type to list of service infos.
    private Map<String, List<ServiceInfo>> serviceCacheMap;

    // Browse listener map. Map service discovery entity type to set of listeners.
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
     * 
     */
    private PlatformSDManager()
    {
        initialized = new AtomicBoolean(false);

        advertiseMap = new HashMap<String, ServiceInfo>();
        serviceCacheMap = new HashMap<String, List<ServiceInfo>>();
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
     *             - throw {@link PlatformException} on error.
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

    public synchronized void shutdown()
    {
        LOG.enterMethod();

        try
        {
            advertiseRWLock.writeLock().lock();

            jmDNSManager.unregisterAllServices();
            advertiseMap.clear();
        }
        finally
        {
            advertiseRWLock.writeLock().unlock();
        }

        try
        {
            browseRWLock.writeLock().lock();
            for (String type : browseListenerMap.keySet())
            {
                jmDNSManager.removeServiceListener(
                    constructServiceType(normalizeServiceType(type), DEFAULT_PROTOCOL_TCP,
                        platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()), this);
            }

            browseListenerMap.clear();
        }
        finally
        {
            browseRWLock.writeLock().unlock();
            LOG.exitMethod();
        }
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
     * Browse for entity on given platform instance.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} implementation to provide callback to notify browse results.
     * @param entityServiceType
     *            - a {@link String} entity service type to browse for.
     * @param browseResultFilter
     *            - an implementation of {@link ISDResultFilter} to filter received results. Optional. Caller should
     *            provide necessary business logic for filtering results. If null, all results will be passed via
     *            provided callback.
     * @throws PlatformException
     *             - throw {@link PlatformException} on browse error.
     */
    public void browse(ISDListener browseResultListener, String entityServiceType, ISDResultFilter browseResultFilter)
        throws PlatformException
    {
        LOG.enterMethod(ARG_BROWSE_LISTENER, browseResultListener, ARG_ENTITY_SERVICE_TYPE, entityServiceType,
            ARG_BROWSE_RESULT_FILTER, browseResultFilter);

        if (!initialized.get())
        {
            throw new PlatformException(ERROR_INITIALIZED);
        }
        try
        {
            ArgsChecker.errorOnNull(browseResultListener, ARG_BROWSE_LISTENER);
            ArgsChecker.errorOnNull(entityServiceType, ARG_ENTITY_SERVICE_TYPE);

            // Check if already browsing for this entity service type.
            boolean isBrowsing = false;
            try
            {
                browseRWLock.writeLock().lock();
                isBrowsing = isBrowsing(entityServiceType);

                // Add listener in any case. Required since we may already be browsing so we don't miss a result that
                // may be received out of sync.
                addBrowseListener(browseResultListener, entityServiceType, browseResultFilter);

                // Start browsing if not already browsing.
                if (!isBrowsing)
                {
                    jmDNSManager.addServiceListener(
                        constructServiceType(normalizeServiceType(entityServiceType), DEFAULT_PROTOCOL_TCP,
                            platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()), this);
                }
            }
            finally
            {
                browseRWLock.writeLock().unlock();
            }

            // Check cache for existing results.
            List<ServiceInfo> resultServiceInfoList = null;
            try
            {
                serviceCacheRWLock.readLock().lock();
                resultServiceInfoList = serviceCacheMap.get(entityServiceType);
            }
            finally
            {
                serviceCacheRWLock.readLock().unlock();
            }

            // Notify with cache result.
            if (resultServiceInfoList != null)
            {
                notifyListener(browseResultListener, entityServiceType, resultServiceInfoList);
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
     * Stop browsing for entity service.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} that is browsing for a service.
     * @param entityServiceType
     *            - a {@link String} entity service type a listener is browsing for.
     * @throws PlatformException
     *             - throw {@link PlatformException} on browse stop error.
     */
    public void browseStop(ISDListener browseResultListener, String entityServiceType) throws PlatformException
    {
        LOG.enterMethod(ARG_BROWSE_LISTENER, browseResultListener, ARG_ENTITY_SERVICE_TYPE, entityServiceType);

        if (!initialized.get())
        {
            throw new PlatformException(ERROR_INITIALIZED);
        }

        try
        {
            ArgsChecker.errorOnNull(browseResultListener, ARG_BROWSE_LISTENER);
            ArgsChecker.errorOnNull(entityServiceType, ARG_ENTITY_SERVICE_TYPE);

            try
            {
                browseRWLock.writeLock().lock();

                removeBrowseListener(browseResultListener, entityServiceType);

                if (!isBrowsing(entityServiceType))
                {
                    jmDNSManager.removeServiceListener(
                        constructServiceType(normalizeServiceType(entityServiceType), DEFAULT_PROTOCOL_TCP,
                            platformSDContextManager.getPlatformId(), platformSDContextManager.getDomain()), this);
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
     * Add new browse result listener if not already present. This method should be called from a synchronized context.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} implementation to provide callback for browse results.
     * @param entityServiceType
     *            - a {@link String} entity service type to browse for.
     * @param browseResultFilter
     *            - an implementation of {@link ISDResultFilter} to filter received results.
     */
    private void addBrowseListener(ISDListener browseResultListener, String entityServiceType,
        ISDResultFilter browseResultFilter)
    {
        // Create browse listener map structure if not already present.
        if (!browseListenerMap.containsKey(entityServiceType))
        {
            browseListenerMap.put(entityServiceType, new HashSet<ISDListener>());
        }
        browseListenerMap.get(entityServiceType).add(browseResultListener);

        if (browseResultFilter != null)
        {
            // Create browse filter map structure if not already present.
            if (!listenerFilterMap.containsKey(browseResultListener))
            {
                listenerFilterMap.put(browseResultListener, new HashMap<String, ISDResultFilter>());
            }
            listenerFilterMap.get(browseResultListener).put(entityServiceType, browseResultFilter);
        }
    }

    /**
     * Remove a browse result listener if present. This method should be called from a synchronized context.
     * 
     * @param browseResultListener
     *            - a {@link ISDListener} implementation to provide callback for browse results.
     * @param entityServiceType
     *            - a {@link String} entity service type to browse for.
     */
    private void removeBrowseListener(ISDListener browseResultListener, String entityServiceType)
    {
        if (browseListenerMap.containsKey(entityServiceType))
        {
            browseListenerMap.get(entityServiceType).remove(browseResultListener);
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
    }

    /**
     * Helper method for constructing JmDNS valid full service type.
     * 
     * @param serviceType
     *            - a {@link String} application service type.
     * @param protocol
     *            - a {@link String} application protocol.
     * @param subDomain
     *            - a {@link String} sub-domain.
     * @param domain
     *            - a {@link String} domain.
     * @return - a {@link String} full service type in format: [<serviceType>._<protocol>.<subDomain>.<domain>.]
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
     * Helper method for detecting whether SD Manager is browsing for given entity service type. This method should be
     * called from a synchronized context.
     * 
     * @param entityServiceType
     *            - a {@link String} entity service type to check if browsing for.
     * @return true if already browsing or false otherwise.
     */
    public boolean isBrowsing(String entityServiceType)
    {
        return browseListenerMap.containsKey(entityServiceType);
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
     * Helper method for converting 3rd party {@link ServiceInfo} elements to internal {@link SDEntityBrowseResult}
     * elements.
     * 
     * @param serviceInfoList
     *            - source {@link List}<{@link ServiceInfo}> of JmDNS resolved services.
     * @return - a {@link List}<{@link SDEntityBrowseResult}> internal elements.
     */
    private List<SDEntityBrowseResult> toSDEntityBrowseResultList(List<ServiceInfo> serviceInfoList)
    {
        List<SDEntityBrowseResult> resultList = new ArrayList<SDEntityBrowseResult>();

        for (ServiceInfo serviceInfo : serviceInfoList)
        {
            Map<String, String> entityContextMap = new HashMap<String, String>();
            Enumeration<String> propertyNames = serviceInfo.getPropertyNames();
            while (propertyNames.hasMoreElements())
            {
                String propertyName = propertyNames.nextElement();
                entityContextMap.put(propertyName, serviceInfo.getPropertyString(propertyName));
            }

            resultList.add(new SDEntityBrowseResult(serviceInfo.getType(), serviceInfo.getName(), entityContextMap,
                serviceInfo.getInet4Addresses()));
        }
        return resultList;
    }

    /**
     * Helper method for normalizing service type to be compatible with JmDNS requirements (prepended with underscore).
     * 
     * @param serviceType
     *            - input {@link String} service type.
     * @return {@link String} underscore prepended service type.
     */
    private String normalizeServiceType(String serviceType)
    {
        return serviceType.startsWith(KEY_UNDERSCORE) ? serviceType : KEY_UNDERSCORE.concat(serviceType);
    }

    @Override
    public void serviceAdded(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_ADDED, event.getName(), event.getType()));
        jmDNSManager.requestServiceInfo(event.getType(), event.getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_REMOVED, event.getName(), event.getType()));

        try
        {
            serviceCacheRWLock.writeLock().lock();
            if (serviceCacheMap.containsKey(event.getInfo().getApplication()))
            {
                serviceCacheMap.get(event.getInfo().getApplication()).remove(event.getInfo());
            }
        }
        finally
        {
            serviceCacheRWLock.writeLock().unlock();
        }
    }

    @Override
    public void serviceResolved(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_RESOLVED, event.getInfo()));

        // Add to cache.
        try
        {
            serviceCacheRWLock.writeLock().lock();
            if (!serviceCacheMap.containsKey(event.getInfo().getApplication()))
            {
                List<ServiceInfo> infoSet = new ArrayList<ServiceInfo>();
                serviceCacheMap.put(event.getInfo().getApplication(), infoSet);
            }
            serviceCacheMap.get(event.getInfo().getApplication()).add(event.getInfo());
        }
        finally
        {
            serviceCacheRWLock.writeLock().unlock();
        }

        // Notify listeners.
        try
        {
            browseRWLock.readLock().lock();
            if (browseListenerMap.containsKey(event.getInfo().getApplication()))
            {
                for (ISDListener listener : browseListenerMap.get(event.getInfo().getApplication()))
                {
                    notifyListener(listener, event.getInfo().getApplication(),
                        Arrays.asList(new ServiceInfo[] { event.getInfo() }));
                }
            }
        }
        finally
        {
            browseRWLock.readLock().unlock();
        }
    }

    /**
     * Notify listener with received results. If filter is present results will be filtered first.
     * 
     * @param sdListener
     *            - a {@link ISDListener} to notify.
     * @param entityServiceType
     *            - a {@link String} entity service type for which results were found.
     * @param serviceInfoList
     *            - a {@link List}<@link ServiceInfo}> of service info results for entity service type that were
     *            discovered. This may contain cached service entries or freshly discovered ones.
     */
    private void notifyListener(ISDListener sdListener, String entityServiceType, List<ServiceInfo> serviceInfoList)
    {
        // Check if we are still browsing (filter still exists).
        ISDResultFilter resultFilter = null;
        try
        {
            browseRWLock.readLock().lock();
            if (listenerFilterMap.containsKey(sdListener))
            {
                resultFilter = listenerFilterMap.get(sdListener).get(entityServiceType);
            }
        }
        finally
        {
            browseRWLock.readLock().unlock();
        }

        List<SDEntityBrowseResult> sdEntityBrowseList = null;
        if (resultFilter != null)
        {
            sdEntityBrowseList = resultFilter.filter(toSDEntityBrowseResultList(serviceInfoList));
        }
        else
        {
            sdEntityBrowseList = toSDEntityBrowseResultList(serviceInfoList);
        }
        if (sdEntityBrowseList != null)
        {
            // Notify listener and guard from listener exceptions.
            try
            {
                sdListener.serviceResolved(sdEntityBrowseList);
            }
            catch (Exception e)
            {
                LOG.warn(WARN_LISTENER_EXCEPTION, e);
            }
            finally
            {
                if (resultFilter instanceof ISDSingleResultFilter)
                {
                    try
                    {
                        browseRWLock.writeLock().lock();
                        removeBrowseListener(sdListener, entityServiceType);
                    }
                    finally
                    {
                        browseRWLock.writeLock().unlock();
                    }
                }
            }
        }
    }
}
