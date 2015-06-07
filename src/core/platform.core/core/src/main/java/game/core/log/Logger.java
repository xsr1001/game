/**
 * @file Logger.java
 * @brief Simple logger wrapper.
 */

package game.core.log;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Simple logger wrapper to provide extended logging functionality.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public final class Logger
{
    // Actual sl4fj logger.
    private final org.slf4j.Logger log;

    // Log messages and errors.
    private static final String MSG_ENTER_METHOD = "Entered method.";
    private static final String MSG_EXIT_METHOD = "Exited method.";
    private static final String MSG_ENTER_PARAMS = "Method parameters:";
    private static final String MSG_EXIT_PARAM = "Exit parameter:";
    private static final String ERROR_ODD_NUMBER_OF_PARAMS = "Invalid number of parameters provided. Expecting even number of arguments.";
    private static final String ERROR_DESCRIBE_PARAMETER = "Invalid even parameter. Expecting even parameters to be object descriptions of type String.";

    /**
     * Ctor.
     * 
     * @param name
     *            - a {@link String} name of the logger.
     */
    public Logger(String name)
    {
        this.log = LoggerFactory.getLogger(name);
    }

    /**
     * Ctor.
     * 
     * @param clazz
     *            - a {@link Class} name for a logger.
     */
    public Logger(Class<?> clazz)
    {
        this.log = LoggerFactory.getLogger(clazz);
    }

    /**
     * Log method entry as trace log.
     */
    public void enterMethod()
    {
        this.log.trace(MSG_ENTER_METHOD);
    }

    /**
     * Log method entry as trace log.
     * 
     * @param arguments
     *            - a variable argument of type {@link Object}. It is expected that the number of arguments is even and
     *            and that each even argument is a {@link String} describing the actual object.
     */
    public void enterMethod(Object... arguments)
    {
        if (arguments == null)
        {
            throw new IllegalArgumentException(ERROR_ODD_NUMBER_OF_PARAMS);
        }
        if (arguments.length % 2 != 0)
        {
            throw new IllegalArgumentException(ERROR_ODD_NUMBER_OF_PARAMS);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(MSG_ENTER_METHOD).append(" ").append(MSG_ENTER_PARAMS).append(System.lineSeparator());
        for (int i = 0; i < arguments.length; ++i)
        {
            if (i % 2 == 0)
            {
                if (!(arguments[i] instanceof String))
                {
                    throw new IllegalArgumentException(ERROR_DESCRIBE_PARAMETER);
                }
                sb.append(String.valueOf(arguments[i])).append(System.lineSeparator());
            }
            else
            {
                sb.append(String.valueOf(arguments[i])).append(':');
            }
        }
        this.log.trace(sb.toString());
    }

    /**
     * Log method exit as trace log.
     */
    public void exitMethod()
    {
        this.log.trace(MSG_EXIT_METHOD);
    }

    /**
     * Log method exit as trace log.
     * 
     * @param description
     *            a {@link String} description of object.
     * @param object
     *            - a {@link Object} to log.
     */
    public void exitMethod(String description, Object object)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(MSG_EXIT_METHOD).append(" ").append(MSG_EXIT_PARAM).append(System.lineSeparator());
        sb.append(description).append(':').append(String.valueOf(object));
        this.log.trace(sb.toString());
    }

    public String getName()
    {
        return this.log.getName();
    }

    public boolean isTraceEnabled()
    {
        return this.log.isTraceEnabled();
    }

    public void trace(String msg)
    {
        this.log.trace(msg);
    }

    public void trace(Object arg)
    {
        this.log.trace("{}", arg);
    }

    public void trace(Object... arguments)
    {
        this.log.trace(generateDummyFormat(arguments.length), arguments);
    }

    public void trace(String msg, Throwable t)
    {
        this.log.trace(msg, t);
    }

    public boolean isTraceEnabled(Marker marker)
    {
        return this.log.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String msg)
    {
        this.log.trace(marker, msg);
    }

    public void trace(Marker marker, String format, Object arg)
    {
        this.log.trace(marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2)
    {
        this.log.trace(marker, format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object... argArray)
    {
        this.log.trace(marker, format, argArray);
    }

    public void trace(Marker marker, String msg, Throwable t)
    {
        this.log.trace(marker, msg, t);
    }

    public boolean isDebugEnabled()
    {
        return this.log.isDebugEnabled();
    }

    public void debug(String msg)
    {
        this.log.debug(msg);
    }

    public void debug(Object arg)
    {
        this.log.debug("{}", arg);
    }

    public void debug(Object... arguments)
    {
        this.log.debug(generateDummyFormat(arguments.length), arguments);
    }

    public void debug(String msg, Throwable t)
    {
        this.log.debug(msg, t);
    }

    public boolean isDebugEnabled(Marker marker)
    {
        return this.log.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg)
    {
        this.log.debug(marker, msg);
    }

    public void debug(Marker marker, String format, Object arg)
    {
        this.log.debug(marker, format, arg);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2)
    {
        this.log.debug(marker, format, arg1, arg2);
    }

    public void debug(Marker marker, String format, Object... argArray)
    {
        this.log.debug(marker, format, argArray);
    }

    public void debug(Marker marker, String msg, Throwable t)
    {
        this.log.debug(marker, msg, t);
    }

    public boolean isInfoEnabled()
    {
        return this.log.isInfoEnabled();
    }

    public void info(String msg)
    {
        this.log.info(msg);
    }

    public void info(Object arg)
    {
        this.log.info("{}", arg);
    }

    public void info(Object... arguments)
    {
        this.log.info(generateDummyFormat(arguments.length), arguments);
    }

    public void info(String msg, Throwable t)
    {
        this.log.info(msg, t);
    }

    public boolean isInfoEnabled(Marker marker)
    {
        return this.log.isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg)
    {
        this.log.info(marker, msg);
    }

    public void info(Marker marker, String format, Object arg)
    {
        this.log.info(marker, format, arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2)
    {
        this.log.info(marker, format, arg1, arg2);
    }

    public void info(Marker marker, String format, Object... argArray)
    {
        this.log.info(marker, format, argArray);
    }

    public void info(Marker marker, String msg, Throwable t)
    {
        this.log.info(marker, msg, t);
    }

    public boolean isWarnEnabled()
    {
        return this.log.isWarnEnabled();
    }

    public void warn(String msg)
    {
        this.log.warn(msg);
    }

    public void warn(Object arg)
    {
        this.log.warn("{}", arg);
    }

    public void warn(Object... arguments)
    {
        this.log.warn(generateDummyFormat(arguments.length), arguments);
    }

    public void warn(String msg, Throwable t)
    {
        this.log.warn(msg, t);
    }

    public boolean isWarnEnabled(Marker marker)
    {
        return this.log.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg)
    {
        this.log.warn(marker, msg);
    }

    public void warn(Marker marker, String format, Object arg)
    {
        this.log.warn(marker, format, arg);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2)
    {
        this.log.warn(marker, format, arg1, arg2);
    }

    public void warn(Marker marker, String format, Object... argArray)
    {
        this.log.warn(marker, format, argArray);
    }

    public void warn(Marker marker, String msg, Throwable t)
    {
        this.log.warn(marker, msg, t);
    }

    public boolean isErrorEnabled()
    {
        return this.log.isErrorEnabled();
    }

    public void error(String msg)
    {
        this.log.error(msg);
    }

    public void error(Object arg)
    {
        this.log.error("{}", arg);
    }

    public void error(Object... arguments)
    {
        this.log.error(generateDummyFormat(arguments.length), arguments);
    }

    public void error(String msg, Throwable t)
    {
        this.log.error(msg, t);
    }

    public boolean isErrorEnabled(Marker marker)
    {
        return this.log.isErrorEnabled(marker);
    }

    public void error(Marker marker, String msg)
    {
        this.log.error(marker, msg);
    }

    public void error(Marker marker, String format, Object arg)
    {
        this.log.error(marker, format, arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2)
    {
        this.log.error(marker, format, arg1, arg2);
    }

    public void error(Marker marker, String format, Object... argArray)
    {
        this.log.error(marker, format, argArray);
    }

    public void error(Marker marker, String msg, Throwable t)
    {
        this.log.error(marker, msg, t);
    }

    /**
     * Helper method for returning dummy log format for given number of arguments.
     * 
     * @param numberOfArgs
     *            - number of log variable place-holders "{} " to create.
     * @return - a {@link String} dummy format.
     */
    private String generateDummyFormat(int numberOfArgs)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfArgs; ++i)
        {
            sb.append("{} ");
        }
        return sb.toString();
    }
}
