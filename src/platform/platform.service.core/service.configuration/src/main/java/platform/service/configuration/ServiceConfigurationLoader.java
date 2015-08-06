/**
 * @file ConfigurationLoader.java
 * @brief Configuration loader.
 */

package platform.service.configuration;

import game.core.api.exception.ConfigurationException;
import game.core.log.Logger;
import game.core.log.LoggerFactory;
import game.core.util.ArgsChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import platform.service.configuration.schema.ServiceConfiguration;

/**
 * Configuration loader. Load any custom service specific configuration file.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class ServiceConfigurationLoader
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfigurationLoader.class);

    // Args, params, errors.
    private static final String ERROR_IO = "I/O error while parsing schema catalog.";
    private static final String ERROR_SCHEMA_CATALOG = "Schema catalog is null or empty.";
    private static final String ERROR_SCHEMA_CATALOG_FORMAT = "Schema catalog format is invalid.";
    private static final String ERROR_CONFIGURATION_INITIALIZATION = "Error initializaing jaxb unmarshaller.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument received.";
    private static final String ERROR_CONFIGURATION_LOAD = "Error loading service configuration file.";
    private static final String ERROR_STREAM_CLOSE = "Error closing input stream.";
    private static final String ARG_CONFIGURATION_FILE = "configurationFile";

    // Service configuration singleton instance.
    private static ServiceConfigurationLoader instance;

    // Current intent is to always parse a single service configuration file within a scope on one JVM. Cache it!
    private ServiceConfiguration cachedServiceConfiguration;

    // Schema catalog. Executable should provide schema catalog on the class path to parse custom service
    // configuration schemas.
    private static final String SCHEMA_CATALOG = "/schema.catalog";

    // JAXB unmarshaller.
    private Unmarshaller jaxbUnmarshaller;

    // External service configuration key. Deployment mechanism should provide this as a JVM argument.
    private static final String EXTERNAL_SERVICE_CONFIGURATION = "service.configuration";

    /**
     * Return singleton instance of {@link ServiceConfigurationLoader}.
     * 
     * @return - singleton instance of {@link ServiceConfigurationLoader}.
     * @throws - ConfigurationException throw {@link ConfigurationException} on initialization error.
     */
    public static synchronized ServiceConfigurationLoader getInstance() throws ConfigurationException
    {
        if (instance == null)
        {
            instance = new ServiceConfigurationLoader();
        }
        return instance;
    }

    /**
     * Private constructor. Parse provided schema catalog to receive capabilities to parse custom service configuration
     * schemas.
     * 
     * @throws ConfigurationException
     *             - throw {@link ConfigurationException} on initialization error.
     */
    private ServiceConfigurationLoader() throws ConfigurationException
    {
        LOG.enterMethod();

        // Parse schema list to construct a master schema for parsing any custom service configuration schema.
        List<String> parsedSchemaList = parseSchemaCatalog();
        validateSchemaList(parsedSchemaList);

        // Create source list and class package list.
        StringBuilder packageStringBuilder = new StringBuilder();
        List<Source> schemaSourceList = new LinkedList<Source>();
        for (String schemaEntry : parsedSchemaList)
        {
            String[] splitSchemaEntry = schemaEntry.split("#");
            schemaSourceList.add(new StreamSource(
                ServiceConfigurationLoader.class.getResourceAsStream(splitSchemaEntry[0])));
            packageStringBuilder.append(splitSchemaEntry[1]).append(":");
        }
        packageStringBuilder.deleteCharAt(packageStringBuilder.length() - 1);

        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(packageStringBuilder.toString());
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source[] sourceArray = new Source[schemaSourceList.size()];
            schemaSourceList.toArray(sourceArray);
            Schema schema = schemaFactory.newSchema(sourceArray);
            jaxbUnmarshaller.setSchema(schema);
        }
        catch (JAXBException jbe)
        {
            LOG.error(ERROR_CONFIGURATION_INITIALIZATION, jbe);
            throw new ConfigurationException(ERROR_CONFIGURATION_INITIALIZATION, jbe);
        }
        catch (SAXException se)
        {
            LOG.error(ERROR_CONFIGURATION_INITIALIZATION, se);
            throw new ConfigurationException(ERROR_CONFIGURATION_INITIALIZATION, se);
        }
        finally
        {
            LOG.exitMethod();
        }
    }

    /**
     * Load external configuration file as declared with JVM argument.
     * 
     * @return - a {@link ServiceConfiguration} parsed configuration file.
     * @throws - ConfigurationException throw {@link ConfigurationException} on configuration parsing error.
     */
    public synchronized ServiceConfiguration loadConfiguration() throws ConfigurationException
    {
        return loadConfiguration(System.getProperty(EXTERNAL_SERVICE_CONFIGURATION), false);
    }

    /**
     * Load provided custom configuration file. User must either provide an absolute path to the file or as an internal
     * resource on the class path.
     * 
     * @param configurationFile
     *            - a {@link String} absolute path to the configuration file or an internal class path resource.
     * @param forceParse
     *            - a flag to clear cached service configuration object.
     * @return - parsed and populated {@link ServiceConfiguration} object.
     * @throws ConfigurationException
     *             - throw {@link ConfigurationException} on configuration parsing error.
     */
    public synchronized ServiceConfiguration loadConfiguration(String configurationFile, boolean forceParse)
        throws ConfigurationException
    {
        LOG.enterMethod(ARG_CONFIGURATION_FILE, configurationFile);
        InputStream inStream = null;
        try
        {
            ArgsChecker.errorOnNull(configurationFile, ARG_CONFIGURATION_FILE);

            if (forceParse)
            {
                cachedServiceConfiguration = null;
            }
            else
            {
                if (cachedServiceConfiguration != null)
                {
                    return cachedServiceConfiguration;
                }
            }

            // First try load it as an external file by its full path.
            File configFile = new File(configurationFile);
            if (configFile.exists())
            {
                inStream = new FileInputStream(configFile);
            }
            else
            {
                inStream = ServiceConfigurationLoader.class.getResourceAsStream(configurationFile);
            }

            ServiceConfiguration configuration = ServiceConfiguration.class.cast(jaxbUnmarshaller.unmarshal(inStream));
            cachedServiceConfiguration = configuration;
            return configuration;
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new ConfigurationException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        catch (FileNotFoundException fnfe)
        {
            LOG.error(ERROR_CONFIGURATION_LOAD, fnfe);
            throw new ConfigurationException(ERROR_CONFIGURATION_LOAD, fnfe);
        }
        catch (JAXBException e)
        {
            LOG.error(ERROR_CONFIGURATION_LOAD, e);
            throw new ConfigurationException(ERROR_CONFIGURATION_LOAD, e);
        }
        catch (ClassCastException cce)
        {
            LOG.error(ERROR_CONFIGURATION_LOAD, cce);
            throw new ConfigurationException(ERROR_CONFIGURATION_LOAD, cce);
        }
        finally
        {
            if (inStream != null)
            {
                try
                {
                    inStream.close();
                }
                catch (IOException ioe)
                {
                    LOG.error(ERROR_STREAM_CLOSE, ioe);
                }
            }
            LOG.exitMethod();
        }
    }

    /**
     * Parse schema catalog to provide schema definitions for service specific configuration files.
     * 
     * @return - a {@link List} of {@link String} entries in schema.catalog file.
     * @throws ConfigurationException
     *             - throw {@link ConfigurationException} on schema catalog parsing error.
     */
    private List<String> parseSchemaCatalog() throws ConfigurationException
    {
        List<String> schemaList = new LinkedList<String>();
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(
                ServiceConfigurationLoader.class.getResourceAsStream(SCHEMA_CATALOG)));
            String line = null;

            while ((line = in.readLine()) != null)
            {
                schemaList.add(line);
            }
        }
        catch (IOException ioe)
        {
            LOG.error(ERROR_IO, ioe);
            throw new ConfigurationException(ERROR_IO, ioe);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                    LOG.error(ERROR_STREAM_CLOSE, ioe);
                }
            }
        }
        return schemaList;
    }

    /**
     * Helper method to validate parsed schema catalog.
     * 
     * @param schemaList
     *            - a {@link List}<@link String}> found line entries in schema catalog.
     * @throws ConfigurationException
     *             - throws {@link ConfigurationException} on failed validation.
     */
    private void validateSchemaList(List<String> schemaList) throws ConfigurationException
    {
        if (schemaList == null || schemaList.isEmpty())
        {
            LOG.error(ERROR_SCHEMA_CATALOG);
            throw new ConfigurationException(ERROR_SCHEMA_CATALOG);
        }

        for (String schemaEntry : schemaList)
        {
            if (schemaEntry.indexOf("#") == -1)
            {
                LOG.error(ERROR_SCHEMA_CATALOG_FORMAT);
                throw new ConfigurationException(ERROR_SCHEMA_CATALOG_FORMAT);
            }
        }
    }
}
