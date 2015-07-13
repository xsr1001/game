/**
 * @file PlatformException.java
 * @brief <description>
 */

package game.core.api.exception;

public class PlatformException extends Exception
{
    private static final long serialVersionUID = -5324268537322325074L;

    public PlatformException(String message)
    {
        super(message);
    }

    public PlatformException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
