/**
 * @file BridgeOptions.java
 * @brief BridgeOptions provides limited bridge related options.
 */

package platform.bridge.api.proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Bridge options provides limited bridge related options. This is just a simple container for options.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class BridgeOptions
{
    // Bridge option keys.
    public static final String KEY_READ_TIMEOUT_SEC = "readTimeoutSec";
    public static final String KEY_SSL_ENABLED = "SSLEnabled";
    public static final String KEY_IS_SERVER = "isServer";
    public static final String KEY_CONNECTION_LISTENER_SET = "connectionListenerSet";

    /**
     * Represents individual bridge option.
     * 
     * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
     *
     * @param <T>
     */
    public class BridgeOption<T>
    {
        // Value of option.
        private T value;

        /**
         * Constructor.
         * 
         * @param value
         *            - A template type value of the option.
         */
        protected BridgeOption(T value)
        {
            this.value = value;
        }

        /**
         * Retrieve value of this option.
         * 
         * @return
         */
        public T get()
        {
            return value;
        }
    }

    // Bridge options container.
    @SuppressWarnings("rawtypes")
    private Map<String, BridgeOption> optionsMap;

    /**
     * Ctor.
     */
    @SuppressWarnings("rawtypes")
    public BridgeOptions()
    {
        optionsMap = new HashMap<String, BridgeOptions.BridgeOption>();
    }

    /**
     * Retrieve a bridge option value.
     * 
     * @param name
     *            - a {@link String} bridge option value.
     * @return - a value of the bridge option.
     */
    @SuppressWarnings("unchecked")
    public <T> BridgeOption<T> get(String name)
    {
        return optionsMap.get(name);
    }

    /**
     * Sets a bridge option.
     * 
     * @param key
     *            - a {@link String} option key.
     * @param value
     *            - a templated type option value.
     */
    public <T> void set(String key, T value)
    {
        optionsMap.put(key, new BridgeOption<T>(value));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append(":").append(System.lineSeparator());

        for (Map.Entry<String, BridgeOption> entry : optionsMap.entrySet())
        {
            sb.append(entry.getKey()).append("-->").append(entry.getValue()).append(System.lineSeparator());
        }

        return sb.toString();
    }
}
