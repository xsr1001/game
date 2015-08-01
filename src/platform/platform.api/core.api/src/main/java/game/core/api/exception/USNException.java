/**
 * @file USNException.java
 * @brief  USN exception represents error in one of the core USN concepts
 */

package game.core.api.exception;

/**
 * USN exception represents error in one of the core USN concepts
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class USNException extends Exception
{
    private static final long serialVersionUID = -5324268537322325074L;

    public USNException(String message)
    {
        super(message);
    }

    public USNException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
