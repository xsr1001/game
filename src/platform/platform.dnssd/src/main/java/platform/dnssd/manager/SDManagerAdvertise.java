/**
 * @file SDManagerAdvertise.java
 * @brief <description>
 */

package platform.dnssd.manager;

import game.core.log.Logger;
import game.core.log.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Service discovery manager for advertise operation.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class SDManagerAdvertise
{
    private static final Logger LOG = LoggerFactory.getLogger(SDManagerAdvertise.class);

    // Errors, args, messages.
    private static final String ERROR_IO_EXCEPTION = "I/O error received advertising service with type: [%s] and name: [%s].";
    private static final String ERROR_ALREADY_ADVERTISED = "Service with type: [%s] and name: [%s] has already been advertised.";
    private static final String MSG_ADVERTISE = "Advertising new service with type: [%s] and name: [%s].";
    private static final String MSG_ADVERTISE_STOP = "Removing advertisement for service with type: [%s] and name: [%s].";
    private static final String ARG_SERVICE_INFO = "serviceInfo";

    // Underlying JmDNS controller.
    private JmDNS jmDNSController;

    // Service advertise set.
    private Set<ServiceInfo> advertiseSet;

    // Synchronization for advertise set
    private ReentrantReadWriteLock advertiseSetRWLock;

    /**
     * Protected constructor. Should be instantiated only from within the package by the top layer service discovery
     * manager.
     * 
     * @param jmDNSController
     *            - an instance of {@link JmDNS} controller for performing service listen requests.
     */
    protected SDManagerAdvertise(JmDNS jmDNSController)
    {
        this.jmDNSController = jmDNSController;

        advertiseSet = new HashSet<ServiceInfo>();
        advertiseSetRWLock = new ReentrantReadWriteLock();
    }

    /**
     * Shutdown service discovery advertise manager and cleanup.
     */
    public void shutdown()
    {
        try
        {
            advertiseSetRWLock.writeLock().lock();

            advertiseSet.clear();
            jmDNSController.unregisterAllServices();
        }
        finally
        {
            advertiseSetRWLock.writeLock().unlock();
        }
    }

    /**
     * Advertise a new service.
     * 
     * @param serviceInfo
     *            - a {@link ServiceInfo} object containing service data to advertise.
     * @return - true if given service has been successfully advertised or false otherwise.
     */
    public boolean advertise(ServiceInfo serviceInfo)
    {
        LOG.enterMethod(ARG_SERVICE_INFO, serviceInfo);
        try
        {
            advertiseSetRWLock.writeLock().lock();
            if (advertiseSet.add(serviceInfo))
            {
                LOG.info(String.format(MSG_ADVERTISE, serviceInfo.getType(), serviceInfo.getName()));
                jmDNSController.registerService(serviceInfo);

                return true;
            }
            else
            {
                LOG.error(String.format(ERROR_ALREADY_ADVERTISED, serviceInfo.getType(), serviceInfo.getName()));
                return false;
            }
        }
        catch (IOException ioe)
        {
            LOG.error(String.format(ERROR_IO_EXCEPTION, serviceInfo), ioe);
            return false;
        }
        finally
        {
            advertiseSetRWLock.writeLock().unlock();
            LOG.exitMethod();
        }
    }

    /**
     * Remove advertisement for given service.
     * 
     * @param serviceInfo
     *            - a {@link ServiceInfo} object containing service data to stop advertising.
     */
    public void advertiseStop(ServiceInfo serviceInfo)
    {
        LOG.enterMethod(ARG_SERVICE_INFO, serviceInfo);
        try
        {
            advertiseSetRWLock.writeLock().lock();
            if (advertiseSet.remove(serviceInfo))
            {
                LOG.info(String.format(MSG_ADVERTISE_STOP, serviceInfo.getType(), serviceInfo.getName()));
                jmDNSController.unregisterService(serviceInfo);
            }
        }
        finally
        {
            advertiseSetRWLock.writeLock().unlock();
            LOG.exitMethod();
        }
    }
}
