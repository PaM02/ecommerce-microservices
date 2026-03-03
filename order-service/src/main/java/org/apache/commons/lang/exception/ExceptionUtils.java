package org.apache.commons.lang.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Minimal shim replacing commons-lang 2.x ExceptionUtils.
 * Only provides the methods used by eureka-client (DiscoveryClient),
 * avoiding the vulnerable ClassUtils from commons-lang 2.6 (CVE-2025-48924).
 */
public class ExceptionUtils {

    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }

    public static String getFullStackTrace(Throwable throwable) {
        return getStackTrace(throwable);
    }

    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private ExceptionUtils() {
    }
}
