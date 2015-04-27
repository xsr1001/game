/**
 * @file ArgsCheckerTest.java
 * @brief Args checker functionality for validating input.
 */

package game.core.util;

/**
 * Utility class for validating arguments. Various checks are provided that throw {@link IllegalArgumentException} on
 * invalid input.
 * 
 * @author Bostjan Lasnik (bostjan.lasnik@hotmail.com)
 *
 */
public class ArgsChecker {
	
	//Args, messages and errors.
    private static final String ERROR_CALLING_METHOD = "Cannot obtain method name from JVM.";
    private static final String ERROR_NO_PARAM_DESCRIPTION = "Null object description provided!";
    private static final String FORMAT_NULL_PARAM = "Input object: [%s] is null in method: [%s].";
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
	static void errorOnNull(Object object, String objectDescription) throws IllegalArgumentException {
		if (objectDescription == null) {
            throw new IllegalArgumentException(ERROR_NO_PARAM_DESCRIPTION);
		}
		
		if (object == null) {
            throw new IllegalArgumentException(String.format(FORMAT_NULL_PARAM, objectDescription, getCallingMethod()));
		}
	}
	
    /**
     * Retrieve calling method and class from the current stack trace associated with this thread if available.
     * 
     * @return - a {@link String} calling class name and method name.
     */
	private static String getCallingMethod() {
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
