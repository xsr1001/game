/**
 * @file BridgeException.java
 * @brief Bridge exception represents any network or bridge specific error.
 */

package platform.core.api.exception;

/**
 * Bridge exception represents any network or bridge specific errors.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class BridgeException extends Exception
{
    private static final long serialVersionUID = -6222047435218031325L;

    public BridgeException(String message)
    {
        super(message);
    }

    public BridgeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
