/**
 * @file LoggerFactory.java
 * @brief Game logger factory.
 */

package game.core.log;

/**
 * Logger factory for internal log wrapper.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class LoggerFactory
{
    /**
     * Return a new instance of game logger with given name.
     * 
     * @param name
     *            - a {@link String} logger name.
     * @return a new instance of {@link Logger}
     */
    public static Logger getLogger(String name)
    {
        return new Logger(name);
    }

    /**
     * Return a new instance of game logger with given class.
     * 
     * @param clazz
     *            - a {@link Class} logger class.
     * @return a new instance of {@link Logger}
     */
    public static Logger getLogger(Class<?> clazz)
    {
        return new Logger(clazz);
    }
}
