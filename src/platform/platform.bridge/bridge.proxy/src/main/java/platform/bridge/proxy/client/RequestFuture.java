/**
 * @file RequestFuture.java
 * @brief Request future represents a result of an asynchronous request to a remote service.
 */

package platform.bridge.proxy.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import platform.bridge.api.protocol.AbstractPacket;
import platform.core.api.exception.BridgeException;

/**
 * Request future represents a result of an asynchronous request to a remote service.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class RequestFuture
{
    // Errors, args, messages.
    private static final String ERROR_INTERRUPTED = "Interrupted exception raised while waiting for a response.";
    private static final String ERROR_TIMEOUT = "Timeout received while waiting for a result.";

    // Asynchronous operation result
    private AbstractPacket result;

    // Wait mechanism to block for a result..
    private CountDownLatch countDownLatch;

    /**
     * Constructor.
     */
    public RequestFuture()
    {
        result = null;
        countDownLatch = new CountDownLatch(1);
    }

    /**
     * Retrieve a result after it has been received. Will wait indefinitely for a result.
     * 
     * @return - a {@link AbstractPacket} received result.
     * @throws BridgeException
     *             - throws {@link BridgeException} if thread is interrupted.
     */
    public AbstractPacket get() throws BridgeException
    {
        try
        {
            countDownLatch.await();
            return result;
        }
        catch (InterruptedException ie)
        {
            throw new BridgeException(ERROR_INTERRUPTED, ie);
        }
    }

    /**
     * Retrieve a result after it has been received. Will wait for a specific amount before throwing exception.
     * 
     * @param timeout
     *            - amount of time to wait for result.
     * @param timeoutUnit
     *            - a {@link TimeUnit} unit of time to wait for result.
     * @return - a {@link AbstractPacket} received result.
     * @throws BridgeException
     *             - throws {@link BridgeException} if thread is interrupted or if block operation has received a
     *             timeout.
     */
    public AbstractPacket get(int timeout, TimeUnit timeoutUnit) throws BridgeException
    {
        try
        {
            if (!countDownLatch.await(timeout, timeoutUnit))
            {
                throw new BridgeException(ERROR_TIMEOUT);
            }
            return result;
        }
        catch (InterruptedException ie)
        {
            throw new BridgeException(ERROR_INTERRUPTED, ie);
        }
    }

    /**
     * Receive a result.
     * 
     * @param result
     *            - a {@link AbstractPacket} asynchronous operation result.
     */
    public void result(AbstractPacket result)
    {
        this.result = result;
        countDownLatch.countDown();
    }

    /**
     * Cancels the blocking operation by decrementing the count down.
     */
    public void cancel()
    {
        countDownLatch.countDown();
    }
}
