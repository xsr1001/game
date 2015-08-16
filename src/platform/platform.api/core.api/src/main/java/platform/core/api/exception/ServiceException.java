/**
 * @file ServiceException.java
 * @brief  Service exception represents general platform service specific exception.
 */

package platform.core.api.exception;

/**
 * Service exception represents general platform service specific exception.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ServiceException extends Exception
{
    private static final long serialVersionUID = 4474296578612818894L;

    public ServiceException(String message)
    {
        super(message);
    }

    public ServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
