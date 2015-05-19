/**
 * @file BridgeException.java
 * @brief <description>
 */

package game.usn.bridge.api;

public class BridgeException extends Exception
{
    public BridgeException(String message)
    {
        super(message);
    }

    public BridgeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
