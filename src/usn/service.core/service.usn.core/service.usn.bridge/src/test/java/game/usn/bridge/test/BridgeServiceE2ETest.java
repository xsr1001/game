/**
 * @file BridgeServiceE2ETest.java
 * @brief <description>
 */

package game.usn.bridge.test;

import game.usn.bridge.USNBridgeManager;
import game.usn.bridge.api.BridgeException;
import game.usn.bridge.api.listener.IChannelListener;
import game.usn.bridge.api.listener.IConnectionListener;
import game.usn.bridge.pipeline.ChannelOptions;
import game.usn.bridge.test.data.TestService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * End to end test for bridge for service proxy.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class BridgeServiceE2ETest
{
    private static ChannelOptions serverOptions;
    private TestService testservice;
    Set<IChannelListener> listenerSet;

    @BeforeClass
    public static void beforeClass()
    {
        Set<IConnectionListener> connSet = new HashSet<IConnectionListener>();
        serverOptions = new ChannelOptions(false, 5, true, connSet);
    }

    @Before
    public void before()
    {
        this.testservice = new TestService();
        this.listenerSet = new HashSet<IChannelListener>();
        this.listenerSet.add(this.testservice);
    }

    @Test
    public void testE2E()
    {
        Socket socket = null;
        try
        {
            USNBridgeManager.getInstance().registerServiceProxy(this.testservice, this.listenerSet, 0, serverOptions);
            while (this.testservice.channelCnt == 0)
            {}
            Assert.assertTrue(this.testservice.channelup);

            socket = new Socket("192.168.0.100", this.testservice.port);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            while (this.testservice.connectedCnt == 0)
            {}
            Assert.assertTrue(this.testservice.connected);
            Assert.assertNotNull(this.testservice.client);
        }
        catch (BridgeException be)
        {
            System.err.println(be);
            Assert.fail();
        }
        catch (IOException e)
        {
            System.err.println(e);
            Assert.fail();
        }
        finally
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                }
                catch (Exception e)
                {

                }

            }
        }
    }
}
