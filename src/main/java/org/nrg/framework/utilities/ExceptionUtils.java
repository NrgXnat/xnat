package org.nrg.framework.utilities;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ExceptionUtils {
    public static String getStackTraceDisplay(final String... patterns) {
        return getStackTraceDisplay(3, patterns);
    }

    public static String getStackTraceDisplay(final int startingDepth, final String... patterns) {
        final StringBuilder       stackTraceDisplay = new StringBuilder();
        final StackTraceElement[] stackTrace        = Thread.currentThread().getStackTrace();
        for (int index = startingDepth; index < stackTrace.length; index++) {
            final StackTraceElement element   = stackTrace[index];
            final String            className = element.getClassName();
            if (Iterables.any(Arrays.asList(patterns), new Predicate<String>() {
                @Override
                public boolean apply(@Nullable final String input) {
                    return StringUtils.isNotBlank(input) && input.matches(className);
                }
            })) {
                stackTraceDisplay.append("    at ").append(className).append(".").append(element.getMethodName()).append("(), line ").append(element.getLineNumber()).append("\n");
            }
        }
        return stackTraceDisplay.toString();
    }
}
