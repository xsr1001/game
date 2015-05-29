/**
 * @file ProtocolException.java
 * @brief <description>
 */

package game.usn.bridge.api;

public class ProtocolException extends Exception
{
    public ProtocolException(String message)
    {
        super(message);
    }

    public ProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
