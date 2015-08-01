/**
 * @file ArgsCheckerTest.java
 * @brief Args checker functionality for validating input.
 */

package game.core.util;

import game.core.log.Logger;
import game.core.log.LoggerFactory;

/**
 * Utility class for validating arguments. Various checks are provided that throw {@link IllegalArgumentException} on
 * invalid input.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ArgsChecker
{
    // Logger.
    private static final Logger LOG = LoggerFactory.getLogger(ArgsChecker.class);

    // Args, messages and errors.
    private static final String ERROR_CALLING_METHOD = "Cannot obtain method name from JVM.";
    private static final String ERROR_NO_PARAM_DESCRIPTION = "Null object description provided!";
    private static final String ERROR_NOT_A_NUMBER = "Object: [%s] with description: [%s] is not a Number in calling method: [%s].";
    private static final String ERROR_CAST_NUMBER = "Object: [%s] with description: [%s] cannot be cast to a Number in calling method: [%s].";
    private static final String FORMAT_NULL_PARAM = "Input object: [%s] is null in method: [%s].";
    private static final String LESS_THAN_0 = "Number: [%s] with description: [%s] is is lesser than 0 in calling method: [%s].";
    private static final String FORMAT_CALLING_METHOD = "%s.%s()";

    /**
     * Validate input to not be null.
     * 
     * @param object
     *            - parameter that is a sub-type of {@link Object} to validate.
     * @param objectDescription
     *            - a {@link String} parameter description.
     * @throws IllegalArgumentException
     *             - throw {@link IllegalArgumentException} on null.
     */
    public static void errorOnNull(Object object, String objectDescription) throws IllegalArgumentException
    {
        if (objectDescription == null)
        {
            LOG.error(ERROR_NO_PARAM_DESCRIPTION);
            throw new IllegalArgumentException(ERROR_NO_PARAM_DESCRIPTION);
        }

        if (object == null)
        {
            LOG.error(String.format(FORMAT_NULL_PARAM, objectDescription, getCallingMethod()));
            throw new IllegalArgumentException(String.format(FORMAT_NULL_PARAM, objectDescription, getCallingMethod()));
        }
    }

    /**
     * Validate input to be greater or equal to 0.
     * 
     * @param object
     *            - parameter that is a sub-type of {@link Object} to validate.
     * @param objectDescription
     *            - a {@link String} parameter description.
     * @throws IllegalArgumentException
     *             - throw {@link IllegalArgumentException} on less than 0.
     */
    public static void errorOnLessThan0(Object object, String objectDescription)
    {
        if (objectDescription == null)
        {
            LOG.error(ERROR_NO_PARAM_DESCRIPTION);
            throw new IllegalArgumentException(ERROR_NO_PARAM_DESCRIPTION);
        }

        if (!(object instanceof Number))
        {
            LOG.error(String.format(ERROR_NOT_A_NUMBER, object, objectDescription, getCallingMethod()));
            throw new IllegalArgumentException(String.format(ERROR_NOT_A_NUMBER, object, objectDescription,
                getCallingMethod()));
        }

        Number number = null;
        try
        {
            number = Number.class.cast(object);
        }
        catch (ClassCastException ce)
        {
            LOG.error(String.format(FORMAT_NULL_PARAM, object, objectDescription, getCallingMethod()), ce);
            throw new IllegalArgumentException(String.format(ERROR_CAST_NUMBER, object, objectDescription,
                getCallingMethod()));
        }

        if (number.longValue() < 0)
        {
            LOG.error(String.format(LESS_THAN_0, number, objectDescription, getCallingMethod()));
            throw new IllegalArgumentException(String.format(LESS_THAN_0, objectDescription, getCallingMethod()));
        }
    }

    /**
     * Retrieve calling method and class from the current stack trace associated with this thread if available.
     * 
     * @return - a {@link String} calling class name and method name.
     */
    private static String getCallingMethod()
    {
        String callingClass = null;
        String callingMethod = null;
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 0th is getClassName() or getMethodName(), 1st is getCallingMethod(), 2nd is ArgsChecker.<static_method> and
        // 3rd is target calling method.
        if (stackTrace.length >= 4)
        {
            callingClass = stackTrace[3].getClassName();
            callingMethod = stackTrace[3].getMethodName();
        }

        if (callingClass == null || callingMethod == null)
        {
            return ERROR_CALLING_METHOD;
        }
        else
        {
            return String.format(FORMAT_CALLING_METHOD, callingClass, callingMethod);
        }
    }
}
