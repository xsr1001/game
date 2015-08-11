/**
 * @file PlatformException.java
 * @brief Platform exception for generic platform error handling.
 */

package platform.core.api.exception;

/**
 * Platform exception for generic platform error handling.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
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
