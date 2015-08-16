/**
 * @file ServiceProxyRegister.java
 * @brief Handles registration and initialization of service proxies to be available during runtime.
 */

package platform.service.infrastructure.proxy;

import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import platform.core.api.exception.BridgeException;
import platform.core.api.exception.ServiceException;
import platform.service.api.IServiceProxy;

/**
 * Service proxy register. Handles registration and initialization of service proxies to be available during runtime.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class ServiceProxyRegister
{
    // Logger.
    protected static final Logger LOG = LoggerFactory.getLogger(ServiceProxyRegister.class);

    // Errors, args, msgs.
    private static final String ERROR_SERVICE_PROXY_REGISTERED = "Service proxy already registered for service type: [%s].";
    private static final String ERROR_SERVICE_PROXY_NOT_REGISTERED = "Service proxy for service type: [%s] has not been registered.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ERROR_INSTANTIATION = "Service proxy cannot be instantiated.";
    private static final String ERROR_PROXY_INITIALIZATION = "Error initializing service proxy.";
    private static final String ERROR_ACCESS = "Service proxy does not provide an accessable or null constructor. ";
    private static final String ARG_SERVICE_TYPE = "serviceType";
    private static final String ARG_SERVICE_PROXY = "serviceProxy";
    private static final String ARG_SERVICE_ADDRESS = "serviceAddress";

    // Singleton instance.
    private static final ServiceProxyRegister INSTANCE = new ServiceProxyRegister();

    // Proxy map, mapping service type to concrete service proxy implementation class.
    private Map<String, Class<? extends IServiceProxy>> proxyMap;

    // Synchronization.
    private ReentrantReadWriteLock proxyMapRWLock;

    /**
     * Singleton getter.
     * 
     * @return - return singleton instance of {@link ServiceProxyRegister}.
     */
    public static ServiceProxyRegister getInstance()
    {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private ServiceProxyRegister()
    {
        proxyMap = new HashMap<String, Class<? extends IServiceProxy>>();
        proxyMapRWLock = new ReentrantReadWriteLock();
    }

    /**
     * Retrieve a fully initialized service proxy. Initialized service proxy is a connected transport channel from this
     * client to a remote service, defined by its service address. TODO: add caching, fail detection.
     * 
     * @param serviceType
     *            - a {@link String} service type for which to retrieve service proxy. Available service types are
     *            defined in {@link IServiceProxy}.
     * @param serviceAddress
     *            - a {@link InetAddress} address of a remote service to connect to.
     * @return - an initialized {@link IServiceProxy} instance with established connection with the remote service.
     * @throws ServiceException
     *             - throw {@link ServiceException} if proxy instance for given type has not been registered or a
     *             connection with remote service cannot be established.
     */
    public synchronized IServiceProxy getServiceProxy(String serviceType, InetAddress serviceAddress)
        throws ServiceException
    {
        LOG.enterMethod(ARG_SERVICE_TYPE, serviceType, ARG_SERVICE_ADDRESS, serviceAddress);

        IServiceProxy serviceProxy = null;
        try
        {
            proxyMapRWLock.writeLock().lock();

            ArgsChecker.errorOnNull(serviceType, ARG_SERVICE_TYPE);
            ArgsChecker.errorOnNull(serviceAddress, ARG_SERVICE_ADDRESS);

            if (!proxyMap.containsKey(serviceType))
            {
                throw new ServiceException(String.format(ERROR_SERVICE_PROXY_NOT_REGISTERED, serviceType));
            }

            Class<? extends IServiceProxy> map = proxyMap.get(serviceType);
            serviceProxy = map.newInstance();
            serviceProxy.initialize(serviceAddress);

            return serviceProxy;
        }
        catch (IllegalAccessException iae)
        {
            LOG.error(ERROR_ACCESS);
            throw new ServiceException(ERROR_ACCESS, iae);
        }
        catch (InstantiationException ie)
        {
            LOG.error(ERROR_INSTANTIATION);
            throw new ServiceException(ERROR_INSTANTIATION, ie);
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT);
            throw new ServiceException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        catch (BridgeException be)
        {
            LOG.error(ERROR_PROXY_INITIALIZATION);
            throw new ServiceException(ERROR_PROXY_INITIALIZATION, be);
        }
        finally
        {
            proxyMapRWLock.writeLock().unlock();
            LOG.exitMethod(ARG_SERVICE_PROXY, serviceProxy);
        }
    }

    /**
     * Register a service proxy class for given service type. Concrete service proxy classes are usually registered at
     * startup.
     * 
     * @param serviceType
     *            - a {@link String} service type for which to register service proxy class. Available service types are
     *            defined in {@link IServiceProxy}.
     * @param serviceProxy
     *            - a class for concrete implementation of {@link IServiceProxy}.
     * @throws ServiceException
     *             - throws {@link ServiceException} if service proxy for given service type has already been
     *             registered.
     */
    public synchronized <T extends IServiceProxy> void registerProxy(String serviceType, Class<T> serviceProxy)
        throws ServiceException
    {
        LOG.enterMethod(ARG_SERVICE_TYPE, serviceType, ARG_SERVICE_PROXY, serviceProxy);
        try
        {
            proxyMapRWLock.writeLock().lock();

            ArgsChecker.errorOnNull(serviceType, ARG_SERVICE_TYPE);
            ArgsChecker.errorOnNull(serviceProxy, ARG_SERVICE_PROXY);

            if (proxyMap.containsKey(serviceType))
            {
                LOG.error(String.format(ERROR_SERVICE_PROXY_REGISTERED, serviceType));
                throw new ServiceException(String.format(ERROR_SERVICE_PROXY_REGISTERED, serviceType));
            }

            proxyMap.put(serviceType, serviceProxy);
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT);
            throw new ServiceException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        finally
        {
            proxyMapRWLock.writeLock().unlock();
            LOG.exitMethod();
        }
    }
}
