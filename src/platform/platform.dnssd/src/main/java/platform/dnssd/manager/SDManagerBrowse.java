/**
 * @file SDManagerBrowse.java
 * @brief Service discovery manager for browse operation.
 */

package platform.dnssd.manager;

import game.core.log.Logger;
import game.core.log.LoggerFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import platform.dnssd.api.filter.ServiceBrowseResult;
import platform.dnssd.listener.ISDBrowseResultListener;

/**
 * Service discovery manager for browse operation.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class SDManagerBrowse implements ServiceListener
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(SDManagerBrowse.class);

    // Errors, args, messages.
    private static final String MSG_SERVICE_ADDED = "Service: [%s --> %s] has been added.";
    private static final String MSG_SERVICE_REMOVED = "Service: [%s --> %s] has been removed.";
    private static final String MSG_SERVICE_RESOLVED = "Service: [%s] has been resolved. Notifying upstream listener.";
    private static final String MSG_BROWSING = "Starting with browse operation for service type: [%s].";
    private static final String MSG_BROWSE_STOP = "Stopping with browse operation for service type: [%s].";
    private static final String ARG_SERVICE_TYPE = "serviceType";

    // Underlying JmDNS controller.
    private JmDNS jmDNSController;

    // Browse result listener.
    private ISDBrowseResultListener browseResultListener;

    // Reference count browsing requests. Map service type to reference count.
    private Map<String, Integer> browseReferenceCountMap;

    // Synchronization for reference count map.
    private ReentrantReadWriteLock browseMapRWLock;

    /**
     * Protected constructor. Should be instantiated only from within the package by the top layer service discovery
     * manager.
     * 
     * @param jmDNSController
     *            - an instance of {@link JmDNS} controller for performing service listen requests.
     * @param sdBrowseResultListener
     *            - an instance of {@link ISDBrowseResultListener} for notifying service discovery results.
     */
    protected SDManagerBrowse(JmDNS jmDNSController, ISDBrowseResultListener sdBrowseResultListener)
    {
        this.jmDNSController = jmDNSController;
        this.browseResultListener = sdBrowseResultListener;

        browseReferenceCountMap = new HashMap<String, Integer>();
        browseMapRWLock = new ReentrantReadWriteLock();
    }

    /**
     * Shutdown service discovery browse manager and cleanup.
     */
    public void shutdown()
    {
        try
        {
            browseMapRWLock.readLock().lock();
            for (String serviceType : browseReferenceCountMap.keySet())
            {
                jmDNSController.removeServiceListener(serviceType, this);
            }

            browseReferenceCountMap.clear();
        }
        finally
        {
            browseMapRWLock.readLock().unlock();
        }
    }

    /**
     * Increase the service type reference count and start browsing if it reaches 1.
     * 
     * @param serviceType
     *            - a fully JmDNS valid {@link String} to start browsing for.
     */
    public void browse(String serviceType)
    {
        LOG.enterMethod(ARG_SERVICE_TYPE, serviceType);
        try
        {
            browseMapRWLock.writeLock().lock();
            if (!browseReferenceCountMap.containsKey(serviceType))
            {
                browseReferenceCountMap.put(serviceType, 0);
            }
            browseReferenceCountMap.put(serviceType, browseReferenceCountMap.get(serviceType) + 1);

            if (browseReferenceCountMap.get(serviceType) == 1)
            {
                serviceType = normalizeServiceType(serviceType);
                LOG.info(String.format(MSG_BROWSING, serviceType));
                jmDNSController.addServiceListener(serviceType, this);
            }
        }
        finally
        {
            browseMapRWLock.writeLock().unlock();
            LOG.exitMethod();
        }
    }

    /**
     * Decrease the service type reference count and stop browsing if it reaches 0.
     * 
     * @param serviceType
     *            - a fully JmDNS valid {@link String} to stop browsing for.
     */
    public void browseStop(String serviceType)
    {
        LOG.enterMethod(ARG_SERVICE_TYPE, serviceType);

        try
        {
            browseMapRWLock.writeLock().lock();
            if (!browseReferenceCountMap.containsKey(serviceType))
            {
                return;
            }
            browseReferenceCountMap.put(serviceType, browseReferenceCountMap.get(serviceType) - 1);

            if (browseReferenceCountMap.get(serviceType) == 0)
            {
                serviceType = normalizeServiceType(serviceType);
                LOG.info(String.format(MSG_BROWSE_STOP, serviceType));
                jmDNSController.removeServiceListener(serviceType, this);
                browseReferenceCountMap.remove(serviceType);
            }
        }
        finally
        {
            browseMapRWLock.writeLock().unlock();
            LOG.exitMethod();
        }
    }

    /**
     * Retrieve a flag whether service discovery manager is currently browsing for given service type.
     * 
     * @param serviceType
     *            - a {@link String} service type to check.
     * @return - true if browsing for given entity service type or false otherwise.
     */
    public boolean isBrowsing(String serviceType)
    {
        try
        {
            browseMapRWLock.readLock().lock();
            return browseReferenceCountMap.containsKey(serviceType);
        }
        finally
        {
            browseMapRWLock.readLock().unlock();
        }
    }

    /**
     * Helper method for normalizing service type to be compatible with JmDNS requirements (prepended with underscore).
     * 
     * @param serviceType
     *            - input {@link String} service type.
     * @return - a {@link String} underscore prepended service type.
     */
    private String normalizeServiceType(String serviceType)
    {
        return serviceType.startsWith("_") ? serviceType : "_".concat(serviceType);
    }

    /**
     * Create a {@link ServiceBrowseResult} object containing resolved service data from a {@link ServiceInfo} object.
     * 
     * @param serviceInfo
     *            - source {@link ServiceInfo} resolved service.
     * @return - a {@link ServiceBrowseResult}> object containing resolved service data.
     */
    private ServiceBrowseResult createServiceBrowseResult(ServiceInfo serviceInfo)
    {
        Map<String, String> serviceContextMap = new HashMap<String, String>();
        Enumeration<String> propertyNames = serviceInfo.getPropertyNames();
        while (propertyNames.hasMoreElements())
        {
            String propertyName = propertyNames.nextElement();
            serviceContextMap.put(propertyName, serviceInfo.getPropertyString(propertyName));
        }

        return new ServiceBrowseResult(serviceInfo.getType(), serviceInfo.getApplication(), serviceInfo.getName(),
            serviceContextMap, serviceInfo.getInet4Addresses());
    }

    @Override
    public void serviceAdded(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_ADDED, event.getName(), event.getType()));
        jmDNSController.requestServiceInfo(event.getType(), event.getName());
    }

    @Override
    public void serviceRemoved(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_REMOVED, event.getName(), event.getType()));
        browseResultListener.notifyServiceRemoved(createServiceBrowseResult(event.getInfo()));
    }

    @Override
    public void serviceResolved(ServiceEvent event)
    {
        LOG.trace(String.format(MSG_SERVICE_RESOLVED, event.getInfo()));
        browseResultListener.notifyServiceResolved(createServiceBrowseResult(event.getInfo()));
    }
}
