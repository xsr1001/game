/**
 * @file ConfigurationException.java
 * @brief <description>
 */

package game.core.api.exception;

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
