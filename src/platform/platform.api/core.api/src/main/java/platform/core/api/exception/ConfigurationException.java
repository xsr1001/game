/**
 * @file ConfigurationException.java
 * @brief Configuration exception for configuration parsing error handling.
 */

package platform.core.api.exception;

/**
 * Configuration exception for configuration parsing error handling.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ConfigurationException extends Exception
{
    private static final long serialVersionUID = -6222047435218031325L;

    public ConfigurationException(String message)
    {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
