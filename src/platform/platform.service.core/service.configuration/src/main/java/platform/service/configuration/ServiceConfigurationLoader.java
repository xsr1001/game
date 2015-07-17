/**
 * @file ConfigurationLoader.java
 * @brief Configuration loader.
 */

package platform.service.configuration;

import game.core.api.exception.ConfigurationException;
import game.core.api.exception.PlatformException;
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
import java.util.ArrayList;
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
import platform.service.configuration.schema.TServiceConfiguration;

/**
 * Configuration loader. Load required service configuration file.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class ServiceConfigurationLoader
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfigurationLoader.class);

    // Args, params, errors.
    private static final String ERROR_CONFIGURATION_INITIALIZATION = "Error initializaing jaxb unmarshaller.";
    private static final String ERROR_IO = "I/O error while parsing schema catalogue.";
    private static final String ERROR_SCHEMA_CATALOGUE = "Schema catalogue is empty.";
    private static final String ERROR_ILLEGAL_ARGUMENT = "Illegal argument received.";
    private static final String ERROR_CONFIGURATION_LOAD = "Error loading service configuration file.";
    private static final String ERROR_STREAM_CLOSE = "Error closing input stream.";
    private static final String ARG_CONFIGURATION_FILE = "configurationFile";

    // Service configuration singleton instance.
    private static ServiceConfigurationLoader instance;

    // External service configuration key.
    private static final String EXTERNAL_SERVICE_CONFIGURATION = "service.configuration";

    // Schema catalogue.
    private static final String SCHEMA_CATALOGUE = "/schema.catalogue";

    // Read schema list.
    private List<String> schemaList;

    // JAXB unmarshaller.
    private Unmarshaller jaxbUnmarshaller;

    /**
     * Return singleton instance of {@link ServiceConfigurationLoader}.
     * 
     * @return singleton instance of {@link ServiceConfigurationLoader}.
     * @throws ConfigurationException
     *             - throw {@link ConfigurationException} on initialization error.
     */
    public static ServiceConfigurationLoader getInstance() throws ConfigurationException
    {
        if (instance == null)
        {
            instance = new ServiceConfigurationLoader();
        }
        return instance;
    }

    /**
     * Private constructor.
     * 
     * @throws ConfigurationException
     *             - throw {@link ConfigurationException} on initialization error.
     */
    private ServiceConfigurationLoader() throws ConfigurationException
    {
        LOG.enterMethod();

        this.schemaList = new LinkedList<String>();

        parseSchemaCatalogue();

        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(TServiceConfiguration.class);
            this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            List<Source> schemaSourceList = new ArrayList<Source>();
            for (String schema : this.schemaList)
            {
                schemaSourceList.add(new StreamSource(ServiceConfigurationLoader.class.getResourceAsStream(schema)));
            }
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
     * Load external configuration file.
     * 
     * @return - parsed and populated {@link ServiceConfiguration} object.
     * @throws PlatformException
     *             - throw {@link PlatformException} on error.
     */
    public ServiceConfiguration loadConfiguration() throws PlatformException
    {
        return loadConfiguration(System.getProperty(EXTERNAL_SERVICE_CONFIGURATION));
    }

    /**
     * Load provided configuration file
     * 
     * @param configurationFile
     *            - a {@link String} path to configuration file.
     * @return - parsed and populated {@link ServiceConfiguration} object.
     * @throws PlatformException
     *             - throw {@link PlatformException} on error.
     */
    public ServiceConfiguration loadConfiguration(String configurationFile) throws PlatformException
    {
        LOG.enterMethod(ARG_CONFIGURATION_FILE, configurationFile);

        InputStream inStream = null;
        try
        {
            ArgsChecker.errorOnNull(configurationFile, ARG_CONFIGURATION_FILE);

            File configFile = new File(configurationFile);
            if (configFile.exists())
            {
                inStream = new FileInputStream(configFile);
            }
            else
            {
                inStream = ServiceConfigurationLoader.class.getResourceAsStream(configurationFile);
            }
            return ServiceConfiguration.class.cast(jaxbUnmarshaller.unmarshal(inStream));
        }
        catch (IllegalArgumentException iae)
        {
            LOG.error(ERROR_ILLEGAL_ARGUMENT, iae);
            throw new PlatformException(ERROR_ILLEGAL_ARGUMENT, iae);
        }
        catch (FileNotFoundException fnfe)
        {
            LOG.error(ERROR_CONFIGURATION_LOAD, fnfe);
            throw new PlatformException(ERROR_CONFIGURATION_LOAD, fnfe);
        }
        catch (JAXBException e)
        {
            LOG.error(ERROR_CONFIGURATION_LOAD, e);
            throw new PlatformException(ERROR_CONFIGURATION_LOAD, e);
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
        }
    }

    /**
     * Parse schema catalogue to provide schema definitions for possible configuration files.
     * 
     * @throws ConfigurationException
     *             - throw {@link ConfigurationException} on schema catalogue parsing error.
     */
    private void parseSchemaCatalogue() throws ConfigurationException
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(
                ServiceConfigurationLoader.class.getResourceAsStream(SCHEMA_CATALOGUE)));
            String line = null;

            while ((line = in.readLine()) != null)
            {
                this.schemaList.add(line);
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

        if (this.schemaList.isEmpty())
        {
            throw new ConfigurationException(ERROR_SCHEMA_CATALOGUE);
        }
    }
}
