package org.nrg.framework.utilities;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExceptionUtils {
    public static String getStackTraceDisplay(final String... patterns) {
        return getStackTraceDisplay(3, patterns);
    }

    public static String getStackTraceDisplay(final int startingDepth, final String... patterns) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.stream(stackTrace, startingDepth, stackTrace.length)
                     .filter(element -> Arrays.stream(patterns).anyMatch(pattern -> pattern.matches(element.getClassName())))
                     .map(element -> "    at " + element.getClassName() + "." + element.getMethodName() + "(), line " + element.getLineNumber())
                     .collect(Collectors.joining("\n"));
    }
}
