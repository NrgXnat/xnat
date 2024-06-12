/*
 * framework: org.nrg.framework.logging.Analytics
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.nrg.framework.analytics.AnalyticsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Analytics {
    public static final String EVENT_KEY = "key";
    public static final Level DEFAULT_LEVEL = Level.TRACE;
    public static final String KEY_STATEMENT = "statement";
    public static final String KEY_THROWABLE = "throwable";
    private static final int MAX_DEPTH = 5;
    public static final String ANALYTICS = "analytics";

    /**
     * Enter the submitted analytics information using the class name as the tool ID. This uses the {@link
     * #DEFAULT_LEVEL default level} as the log level to submit.
     * @param clazz        The class submitting the analytics data.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     */
    public static void enter(Class<?> clazz, String statement) {
        enter(clazz.getName(), DEFAULT_LEVEL, wrapStatement(statement));
    }

    /**
     * Enter the submitted throwable as analytics data using the class name as the tool ID. This uses the {@link
     * Level#ERROR} log level as the log level to submit.
     * @param clazz        The class submitting the analytics data.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(Class<?> clazz, Throwable throwable) {
        enter(clazz.getName(), Level.ERROR, wrapStatement(throwable));
    }

    /**
     * Enter the submitted analytics data and throwable using the class name as the tool ID. This uses the
     * {@link Level#ERROR} log level as the log level to submit.
     * @param clazz        The class submitting the analytics data.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(Class<?> clazz, String statement, Throwable throwable) {
        enter(clazz.getName(), Level.ERROR, wrapStatement(statement, throwable));
    }

    /**
     * Enter the submitted analytics information using the class name as the tool ID.
     * @param clazz        The class submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     */
    public static void enter(Class<?> clazz, Level level, String statement) {
        enter(clazz.getName(), level, wrapStatement(statement));
    }

    /**
     * Enter the submitted throwable as analytics data using the class name as the tool ID at the indicated log level.
     * @param clazz        The class submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(Class<?> clazz, Level level, Throwable throwable) {
        enter(clazz.getName(), level, wrapStatement(throwable));
    }

    /**
     * Enter the submitted analytics information and throwable using the class name as the tool ID at the indicated log
     * level.
     * @param clazz        The class submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(Class<?> clazz, Level level, String statement, Throwable throwable) {
        enter(clazz.getName(), level, wrapStatement(statement, throwable));
    }

    /**
     * Enter the submitted analytics information using the class name as the tool ID at the default log level.
     * @param clazz        The class submitting the analytics data.
     * @param properties   The properties to be logged as analytics data.
     */
    public static void enter(Class<?> clazz, Map<String, String> properties) {
        enter(clazz.getName(), DEFAULT_LEVEL, properties);
    }

    /**
     * Enter the submitted analytics information using the class name as the tool ID at the indicated log level.
     * @param clazz        The class submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param properties   The properties to be logged as analytics data.
     */
    public static void enter(Class<?> clazz, Level level, Map<String, String> properties) {
        enter(clazz.getName(), level, properties);
    }

    /**
     * Enter the submitted analytics information using the provided tool ID. This uses the {@link #DEFAULT_LEVEL default
     * level} as the log level to submit.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     */
    public static void enter(String tool, String statement) {
        enter(tool, DEFAULT_LEVEL, wrapStatement(statement));
    }

    /**
     * Enter the submitted throwable as analytics data using the provided tool ID. This uses the {@link Level#ERROR}
     * log level as the log level to submit.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(String tool, Throwable throwable) {
        enter(tool, Level.ERROR, wrapStatement(throwable));
    }

    /**
     * Enter the submitted analytics data and throwable using the provided tool ID. This uses the {@link Level#ERROR}
     * log level as the log level to submit.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(String tool, String statement, Throwable throwable) {
        enter(tool, Level.ERROR, wrapStatement(statement, throwable));
    }

    /**
     * Enter the submitted analytics information using the provided tool ID.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     */
    public static void enter(String tool, Level level, String statement) {
        enter(tool, level, wrapStatement(statement));
    }

    /**
     * Enter the submitted throwable as analytics data using the provided tool ID at the indicated log level.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(String tool, Level level, Throwable throwable) {
        enter(tool, level, wrapStatement(throwable));
    }

    /**
     * Enter the submitted analytics information and throwable using the provided tool ID at the indicated log level.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param statement    The statement to submit. The meaning of this statement is dependent on the type of analytics
     *                     data being submitted.
     * @param throwable    The throwable object to submit.
     */
    public static void enter(String tool, Level level, String statement, Throwable throwable) {
        enter(tool, level, wrapStatement(statement, throwable));
    }

    /**
     * Enter the submitted analytics information using the provided tool ID at the default log level.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param properties   The properties to be logged as analytics data.
     */
    public static void enter(String tool, Map<String, String> properties) {
        Level level;
        if (properties.containsKey("level")) {
            level = Level.valueOf(properties.get("level"));
        } else {
            level = DEFAULT_LEVEL;
        }
        enter(tool, level, properties);
    }

    /**
     * Enter the submitted analytics information using the provided tool ID at the indicated log level.
     * @param tool         The ID for the tool submitting the analytics data.
     * @param level        The log level at which to submit.
     * @param properties   The properties to be logged as analytics data.
     */
    public static void enter(String tool, Level level, Map<String, String> properties) {
        try {
            final AnalyticsEvent event = new AnalyticsEvent();
            event.setKey(tool);
            event.setLevel(level);
            event.setProperties(properties);
            final String payload = _serializer.writeValueAsString(event);
            switch (level) {
                case TRACE:
                    _analytics.trace(payload);
                    break;
                case DEBUG:
                    _analytics.debug(payload);
                    break;
                case INFO:
                    _analytics.info(payload);
                    break;
                case WARN:
                    _analytics.warn(payload);
                    break;
                default:
                    _analytics.error(payload);
            }
        } catch (IOException exception) {
            _log.error("Error during analytics serialization", exception);
        }
    }

    /**
     * Wraps the statement in a map.
     * @param statement    The statement to wrap.
     * @return A map containing the provided statement mapped to the {@link #KEY_STATEMENT statement key}.
     */
    private static Map<String, String> wrapStatement(final String statement) {
        Map<String, String> properties = new HashMap<>();
        properties.put(KEY_STATEMENT, statement);
        return properties;
    }

    /**
     * Wraps the throwable in a map. The throwable is converted using the {@link #convertThrowable(Throwable)} method.
     * @param throwable    The throwable to wrap.
     * @return A map containing the provided throwable mapped to the {@link #KEY_THROWABLE throwable key}.
     */
    private static Map<String, String> wrapStatement(final Throwable throwable) {
        Map<String, String> properties = new HashMap<>();
        properties.put(KEY_THROWABLE, convertThrowable(throwable));
        return properties;
    }

    /**
     * Wraps the statement and throwable in a map. The throwable is converted using the
     * {@link #convertThrowable(Throwable)} method.
     * @param statement    The statement to wrap.
     * @param throwable    The throwable to wrap.
     * @return A map containing the provided statement mapped to the {@link #KEY_STATEMENT statement key} and the
     * provided throwable mapped to the {@link #KEY_THROWABLE throwable key}.
     */
    private static Map<String, String> wrapStatement(final String statement, final Throwable throwable) {
        Map<String, String> properties = new HashMap<>();
        properties.put("statement", statement);
        properties.put("throwable", convertThrowable(throwable));
        return properties;
    }

    /**
     * Converts the throwable to a string.
     * @param throwable    The throwable to convert to a string.
     * @return The converted throwable.
     */
    private static String convertThrowable(Throwable throwable) {
        return convertThrowable(0, throwable);
    }

    /**
     * Converts the throwable to a string. The integer parameter indicates the depth of the throwable. The first two
     * causes will have full stack traces exposed. Any nested causes up to {@link #MAX_DEPTH} will have the throwable
     * class name.
     * @param depth        The depth of the current throwable.
     * @param throwable    The throwable to convert to a string.
     * @return The converted throwable.
     */
    private static String convertThrowable(int depth, Throwable throwable) {
        StringBuilder buffer = new StringBuilder();
        if (depth == 0) {
            buffer.append("Found throwable exception of type: ");
        }
        buffer.append(throwable.getClass()).append("\n").append(throwable.getMessage()).append("\n");
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace != null) {
            for (StackTraceElement element : stackTrace) {
                buffer.append(element).append("\n");
            }
        }
        if (throwable.getCause() != null) {
            buffer.append("Caused by: ");
            if (depth <= 1) {
                buffer.append(convertThrowable(depth + 1, throwable.getCause()));
            } else if (depth < MAX_DEPTH) {
                buffer.append(convertThrowable(depth + 1, throwable.getCause()));
            } else {
                buffer.append(throwable.getCause().getClass().getName()).append("...");
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    /**
     * This log data member is for analytics functional logging.
     */
    private static final Logger _log = LoggerFactory.getLogger(Analytics.class);
    /**
     * This log data member is for actually dispatching analytics data.
     */
    private static final Logger _analytics = LoggerFactory.getLogger(ANALYTICS);
    /**
     * Object mapper for managing JSON serialization. Can be overridden for DI for greater flexibility if necessary later.
     */
    private static final ObjectMapper _serializer = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
}
