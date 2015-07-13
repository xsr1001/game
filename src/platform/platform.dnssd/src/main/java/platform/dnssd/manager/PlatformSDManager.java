/**
 * @file PlatformSDManager.java
 * @brief Platform service discovery manager.
 */

package platform.dnssd.manager;

import game.core.api.exception.PlatformException;
import game.core.api.exception.USNException;
import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import platform.dnssd.api.IPlatformSDContextManager;
import platform.dnssd.api.endpoint.ISDEntity;
import platform.dnssd.api.filter.ISDBrowseFilter;
import platform.dnssd.api.listener.ISDListener;

/**
 * Platform service discovery manager. Provides functionality for browsing and advertising entities via multi-cast DNS
 * on given platform instance.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class PlatformSDManager
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(PlatformSDManager.class);

    // Errors, args, messages.
    private static final String ERROR_IO_EXCEPTION = "I/O error received while using JmDNS manager.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument provided.";
    private static final String ARG_ENTITY_NETWORK_PORT = "sdEntityNetworkPort";
    private static final String ARG_ENTITY_NAME = "sdEntityName";
    private static final String ARG_ENTITY = "sdEntity";
    private static final String ARG_ENTITY_CONTEXT = "sdEntityContext";
    private static final String ARG_SD_CONTEXT_MANAGER = "platformSDContextManager";
    private static final String MSG_ADVERTISING = "Advertising new service discovery entity: [%s] on current platform.";
    private static final String MSG_ALREADY_ADVERTISING = "Service discovery entity with type: [%s] is already being advertised.";

    // Service type keys.
    private static final String DEFAULT_PROTOCOL_TCP = "tcp";
    private static final String KEY_UNDERSCORE = "_";
    private static final String KEY_DOT_DELIMETER = ".";

    // Singleton instance.
    private static PlatformSDManager instance;

    // Determines if PlatformSDManager is initialized.
    private AtomicBoolean initialized;

    // Platform service discovery context manager.
    private IPlatformSDContextManager platformSDContextManager;

    // Service discovery controller.
    public JmDNS jmDNSManager;

    // Synchronization mechanisms.
    private ReentrantReadWriteLock advertiseRWLock;

    // Advertise map. Maps service discovery entity type to ServiceInfo.
    private Map<String, ServiceInfo> advertiseMap;

    /**
     * Singleton getter.
     * 
     * @param platformSDContextManager
     *            - implementation of {@link IPlatformSDContextManager} to provide platform specific context.
     * @return - the only instance of {@link USNSDManager}.
     * @throws USNException
     *             - throw a {@link PlatformException} on invalid {@link IPlatformSDContextManager} instance.
     */
    public synchronized static PlatformSDManager getInstance(IPlatformSDContextManager platformSDContextManager)
        throws PlatformException
    {
        LOG.enterMethod(ARG_SD_CONTEXT_MANAGER, platformSDContextManager);
        try
        {
            if (instance != null)
            {
                return instance;
            }
            ArgsChecker.errorOnNull(platformSDContextManager, ARG_SD_CONTEXT_MANAGER);
            instance = new PlatformSDManager(platformSDContextManager);

            return instance;
        }
        catch (IllegalArgumentException ie)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, ie);
            throw new PlatformException(ERROR_ILLEGAL_ARGUMENT, ie);
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
     *            - implementation of {@link platformSDContextManager} to provide platform specific context.
     */
    private PlatformSDManager(IPlatformSDContextManager platformSDContextManager)
    {
        this.initialized = new AtomicBoolean(false);
        this.platformSDContextManager = platformSDContextManager;

        this.advertiseMap = new HashMap<String, ServiceInfo>();
        this.advertiseRWLock = new ReentrantReadWriteLock();
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
            this.jmDNSManager = JmDNS.create();
            this.initialized.set(true);
        }
        LOG.exitMethod();
    }

    /**
     * Advertise new service discovery entity on platform.
     * 
     * @param sdEntity
     *            - a source {@link ISDEntity} to advertise.
     * @param sdEntityName
     *            - a {@link String} sd entity name. Name may be changed by underlying manager to preserve uniqueness.
     * @param sdEntityNetworkPort
     *            - a valid network port which sd entity is listening on.
     * @param sdEntityContext
     *            - a {@link Map}<{@link String}, {@link String}> map providing custom sd entity context to advertise.
     * @return true if service discovery entity has been successfully advertised or false otherwise.
     * @throws PlatformException
     *             - throw {@link PlatformException} on error.
     */
    public synchronized boolean advertise(final ISDEntity sdEntity, final String sdEntityName, int sdEntityNetworkPort,
        final Map<String, String> sdEntityContext) throws PlatformException
    {
        LOG.enterMethod(ARG_ENTITY, sdEntity, ARG_ENTITY_NAME, sdEntityName, ARG_ENTITY_NETWORK_PORT,
            sdEntityNetworkPort, ARG_ENTITY_CONTEXT, sdEntityContext);
        try
        {
            ArgsChecker.errorOnNull(sdEntity, ARG_ENTITY);
            ArgsChecker.errorOnNull(sdEntityName, ARG_ENTITY_NAME);
            doSanityCheck();

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
                    this.platformSDContextManager.getPlatformId(), this.platformSDContextManager.getDomain()),
                sdEntityName, sdEntityNetworkPort, 0, 0, true, propertyMap);

            LOG.info(String.format(MSG_ADVERTISING, serviceInfo.toString()));
            this.jmDNSManager.registerService(serviceInfo);

            try
            {
                this.advertiseRWLock.writeLock().lock();
                this.advertiseMap.put(sdEntity.getEntityServiceType(), serviceInfo);
            }
            finally
            {
                this.advertiseRWLock.writeLock().unlock();
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
        try
        {
            ArgsChecker.errorOnNull(sdEntity, ARG_ENTITY);
            doSanityCheck();

            if (!isAdvertised(sdEntity))
            {
                return;
            }

            ServiceInfo serviceInfo = this.advertiseMap.get(sdEntity.getEntityServiceType());
            this.jmDNSManager.unregisterService(serviceInfo);

            try
            {
                this.advertiseRWLock.writeLock().lock();
                this.advertiseMap.remove(sdEntity.getEntityServiceType());
            }
            finally
            {
                this.advertiseRWLock.writeLock().unlock();
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

    public void browse(ISDListener browseResultListener, String sdEntityType, ISDBrowseFilter browseFilter)
    {

    }

    public void browseStop(ISDListener browseResultListener)
    {

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
            this.advertiseRWLock.readLock().lock();
            return this.advertiseMap.containsKey(sdEntity.getEntityServiceType());
        }
        finally
        {
            this.advertiseRWLock.readLock().unlock();
            LOG.exitMethod();
        }
    }
}
