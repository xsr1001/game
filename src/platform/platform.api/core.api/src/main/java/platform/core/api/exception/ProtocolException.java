/**
 * @file ProtocolException.java
 * @brief  Protocol exception represents violation of data contract between platform services.
 */

package platform.core.api.exception;

/**
 * Protocol exception represents violation of data contract between platform services.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ProtocolException extends Exception
{
    private static final long serialVersionUID = -5324268537322325074L;

    public ProtocolException(String message)
    {
        super(message);
    }

    public ProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
